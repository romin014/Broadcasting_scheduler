package com.example.broadcasting_scheduler.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.broadcasting_scheduler.member.Member;
import com.example.broadcasting_scheduler.member.MemberRepository;
import com.example.broadcasting_scheduler.member.Specialty;
import com.example.broadcasting_scheduler.stats.StatsService;

@Service
@Transactional
public class ScheduleService {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final StatsService statsService;

    public ScheduleService(MemberRepository memberRepository,
            ScheduleRepository scheduleRepository,
            StatsService statsService) {
        this.memberRepository = memberRepository;
        this.scheduleRepository = scheduleRepository;
        this.statsService = statsService;
    }

    /**
     * 기존 스케줄 삭제 후 새로 생성
     */
    public void generateMonthlySchedule(int year, int month, Map<Long, Map<String, Boolean>> memberAvailability) {
        // 전월 통계 업데이트
        statsService.updateLastMonthCounts(year, month);

        // 기존 스케줄 삭제
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Schedule> existingSchedules = scheduleRepository.findByDateBetween(start, end);
        scheduleRepository.deleteAll(existingSchedules);

        List<Member> members = memberRepository.findAll();

        // 멤버별 가용성 업데이트
        if (memberAvailability != null) {
            for (Map.Entry<Long, Map<String, Boolean>> entry : memberAvailability.entrySet()) {
                Long memberId = entry.getKey();
                Map<String, Boolean> availability = entry.getValue();
                
                Member member = memberRepository.findById(memberId).orElse(null);
                if (member != null) {
                    // Specialty 업데이트
                    boolean canWeekday = availability.getOrDefault("weekday", true);
                    boolean canSunday = availability.getOrDefault("sunday", true);
                    boolean canDawn = availability.getOrDefault("dawn", true);
                    
                    if (canWeekday && canSunday && canDawn) {
                        member.setSpecialty(Specialty.ALL);
                    } else if (canWeekday) {
                        member.setSpecialty(Specialty.WEEKDAY);
                    } else if (canSunday) {
                        member.setSpecialty(Specialty.SUNDAY);
                    } else if (canDawn) {
                        member.setSpecialty(Specialty.DAWN);
                    }
                    memberRepository.save(member);
                }
            }
        }

        // 현재 달 투입 횟수 기록
        Map<Long, Integer> currentCount = new HashMap<>();
        for (Member m : members) {
            currentCount.put(m.getId(), 0);
        }

        // 스케줄 생성
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            for (WorshipType worshipType : getWorshipTypes(date)) {
                Member selected = selectMember(
                        worshipType,
                        members,
                        currentCount,
                        memberAvailability);

                if (selected == null)
                    continue;

                scheduleRepository.save(
                        new Schedule(date, worshipType, selected));

                currentCount.put(
                        selected.getId(),
                        currentCount.get(selected.getId()) + 1);
            }
        }

        // 통계 저장
        statsService.calculateAndSaveMonthlyStats(year, month);
    }

    /**
     * 기존 스케줄 조회
     */
    public List<Schedule> getMonthlySchedule(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return scheduleRepository.findByDateBetween(start, end);
    }

    private List<WorshipType> getWorshipTypes(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return List.of(WorshipType.SUNDAY);
        }
        return List.of(WorshipType.WEEKDAY, WorshipType.DAWN);
    }

    private Member selectMember(
            WorshipType worshipType,
            List<Member> members,
            Map<Long, Integer> currentCount,
            Map<Long, Map<String, Boolean>> memberAvailability) {

        List<Member> availableMembers = members.stream()
                .filter(m -> canServe(m, worshipType, memberAvailability))
                .collect(Collectors.toList());

        if (availableMembers.isEmpty()) {
            return null;
        }

        // 전월 횟수가 적은 순, 현재 달 횟수가 적은 순으로 정렬
        return availableMembers.stream()
                .sorted(
                        Comparator
                                .comparingInt(Member::getLastMonthCount)
                                .thenComparingInt(m -> currentCount.get(m.getId())))
                .findFirst()
                .orElse(null);
    }

    private boolean canServe(Member member, WorshipType worshipType, Map<Long, Map<String, Boolean>> memberAvailability) {
        // 가용성 정보가 있으면 확인
        if (memberAvailability != null && memberAvailability.containsKey(member.getId())) {
            Map<String, Boolean> availability = memberAvailability.get(member.getId());
            if (worshipType == WorshipType.WEEKDAY && !availability.getOrDefault("weekday", true)) {
                return false;
            }
            if (worshipType == WorshipType.SUNDAY && !availability.getOrDefault("sunday", true)) {
                return false;
            }
            if (worshipType == WorshipType.DAWN && !availability.getOrDefault("dawn", true)) {
                return false;
            }
        }

        Specialty s = member.getSpecialty();

        if (s == Specialty.ALL)
            return true;

        if (s == Specialty.WEEKDAY && worshipType == WorshipType.WEEKDAY)
            return true;
        if (s == Specialty.SUNDAY && worshipType == WorshipType.SUNDAY)
            return true;
        if (s == Specialty.DAWN && worshipType == WorshipType.DAWN)
            return true;

        return false;
    }
}
