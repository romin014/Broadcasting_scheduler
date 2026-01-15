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
import com.example.broadcasting_scheduler.member.MemberAvailability;
import com.example.broadcasting_scheduler.member.MemberAvailabilityRepository;
import com.example.broadcasting_scheduler.member.Specialty;
import com.example.broadcasting_scheduler.stats.StatsService;

@Service
@Transactional
public class ScheduleService {

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final StatsService statsService;
    private final DayOfWeekSettingRepository dayOfWeekSettingRepository;
    private final SundaySettingRepository sundaySettingRepository;
    private final MemberAvailabilityRepository memberAvailabilityRepository;

    public ScheduleService(MemberRepository memberRepository,
            ScheduleRepository scheduleRepository,
            StatsService statsService,
            DayOfWeekSettingRepository dayOfWeekSettingRepository,
            SundaySettingRepository sundaySettingRepository,
            MemberAvailabilityRepository memberAvailabilityRepository) {
        this.memberRepository = memberRepository;
        this.scheduleRepository = scheduleRepository;
        this.statsService = statsService;
        this.dayOfWeekSettingRepository = dayOfWeekSettingRepository;
        this.sundaySettingRepository = sundaySettingRepository;
        this.memberAvailabilityRepository = memberAvailabilityRepository;
    }

    /**
     * 기존 스케줄 삭제 후 새로 생성
     */
    public void generateMonthlySchedule(int year, int month, Map<Long, Map<String, Boolean>> memberAvailability,
            Map<Integer, Map<String, Boolean>> dayOfWeekSettings, Map<String, Boolean> sundaySettings) {
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

        // 요일별 설정 저장
        if (dayOfWeekSettings != null) {
            for (Map.Entry<Integer, Map<String, Boolean>> entry : dayOfWeekSettings.entrySet()) {
                int dayOfWeek = entry.getKey();
                Map<String, Boolean> settings = entry.getValue();
                boolean weekdayEnabled = settings.getOrDefault("weekday", true);
                boolean dawnEnabled = settings.getOrDefault("dawn", true);

                DayOfWeekSetting setting = dayOfWeekSettingRepository.findByDayOfWeek(dayOfWeek)
                        .orElse(new DayOfWeekSetting(dayOfWeek, weekdayEnabled, dawnEnabled));
                setting.setWeekdayEnabled(weekdayEnabled);
                setting.setDawnEnabled(dawnEnabled);
                dayOfWeekSettingRepository.save(setting);
            }
        }

        // 주일 예배 설정 저장
        if (sundaySettings != null) {
            boolean sunday1Enabled = sundaySettings.getOrDefault("sunday1", true);
            boolean sunday2Enabled = sundaySettings.getOrDefault("sunday2", true);
            boolean sunday3Enabled = sundaySettings.getOrDefault("sunday3", true);

            SundaySetting setting = sundaySettingRepository.findFirstByOrderByIdAsc()
                    .orElse(new SundaySetting(sunday1Enabled, sunday2Enabled, sunday3Enabled));
            setting.setSunday1Enabled(sunday1Enabled);
            setting.setSunday2Enabled(sunday2Enabled);
            setting.setSunday3Enabled(sunday3Enabled);
            sundaySettingRepository.save(setting);
        }

        // 스케줄 생성
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            for (WorshipType worshipType : getWorshipTypes(date)) {
                int slots = getSlotCountFor(worshipType, date);
                java.util.Set<Long> selectedThisSlot = new java.util.HashSet<>();

                for (int s = 0; s < slots; s++) {
                    Member selected = selectMember(
                            date,
                            worshipType,
                            members,
                            currentCount,
                            memberAvailability,
                            selectedThisSlot);

                    if (selected == null)
                        break;

                    scheduleRepository.save(new Schedule(date, worshipType, selected));

                    currentCount.put(selected.getId(), currentCount.get(selected.getId()) + 1);
                    selectedThisSlot.add(selected.getId());
                }
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
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayOfWeekValue = dayOfWeek.getValue(); // 1=월요일, 7=일요일

        // 일요일은 주일 예배 1부, 2부, 3부
        if (dayOfWeekValue == 7) {
            SundaySetting setting = sundaySettingRepository.findFirstByOrderByIdAsc()
                    .orElse(new SundaySetting(true, true, true)); // 기본값: 모두 활성화

            List<WorshipType> types = new java.util.ArrayList<>();
            if (setting.isSunday1Enabled()) {
                types.add(WorshipType.SUNDAY_1);
            }
            if (setting.isSunday2Enabled()) {
                types.add(WorshipType.SUNDAY_2);
            }
            if (setting.isSunday3Enabled()) {
                types.add(WorshipType.SUNDAY_3);
            }
            return types;
        }

        // 요일별 설정 확인
        DayOfWeekSetting setting = dayOfWeekSettingRepository.findByDayOfWeek(dayOfWeekValue)
                .orElse(new DayOfWeekSetting(dayOfWeekValue, true, true)); // 기본값: 모두 활성화

        List<WorshipType> types = new java.util.ArrayList<>();
        if (setting.isWeekdayEnabled()) {
            types.add(WorshipType.WEEKDAY);
        }
        if (setting.isDawnEnabled()) {
            types.add(WorshipType.DAWN);
        }

        return types;
    }

    private int getSlotCountFor(WorshipType worshipType, LocalDate date) {
        switch (worshipType) {
            case WEEKDAY:
                // 토요일은 1명, 평일은 2명
                return date.getDayOfWeek().getValue() == 6 ? 1 : 2;
            case SUNDAY_1:
                return 2;
            case SUNDAY_2:
                return 2;
            case SUNDAY_3:
                return 3;
            case DAWN:
            default:
                return 1;
        }
    }

    private Member selectMember(
            LocalDate date,
            WorshipType worshipType,
            List<Member> members,
            Map<Long, Integer> currentCount,
            Map<Long, Map<String, Boolean>> memberAvailability,
            java.util.Set<Long> excludedMemberIds) {

        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=월요일, 7=일요일

        List<Member> availableMembers = members.stream()
                .filter(m -> (excludedMemberIds == null || !excludedMemberIds.contains(m.getId())))
                .filter(m -> canServe(m, date, dayOfWeek, worshipType, memberAvailability))
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

    private boolean canServe(Member member, LocalDate date, int dayOfWeek, WorshipType worshipType,
            Map<Long, Map<String, Boolean>> memberAvailability) {
        // 멤버별 요일별 가용성 확인
        MemberAvailability memberAvail = memberAvailabilityRepository.findByMemberAndDayOfWeek(member, dayOfWeek)
                .orElse(null);

        if (memberAvail != null) {
            // 요일별 가용성 설정이 있으면 확인
            if (worshipType == WorshipType.WEEKDAY && !memberAvail.isWeekdayEnabled()) {
                return false;
            }
            if (worshipType == WorshipType.DAWN && !memberAvail.isDawnEnabled()) {
                return false;
            }
            if (worshipType == WorshipType.SUNDAY_1 && !memberAvail.isSunday1Enabled()) {
                return false;
            }
            if (worshipType == WorshipType.SUNDAY_2 && !memberAvail.isSunday2Enabled()) {
                return false;
            }
            if (worshipType == WorshipType.SUNDAY_3 && !memberAvail.isSunday3Enabled()) {
                return false;
            }
        } else {
            // 요일별 가용성 설정이 없으면 기본 가용성 정보 확인
            if (memberAvailability != null && memberAvailability.containsKey(member.getId())) {
                Map<String, Boolean> availability = memberAvailability.get(member.getId());
                if (worshipType == WorshipType.WEEKDAY && !availability.getOrDefault("weekday", true)) {
                    return false;
                }
                if ((worshipType == WorshipType.SUNDAY_1 || worshipType == WorshipType.SUNDAY_2
                        || worshipType == WorshipType.SUNDAY_3)
                        && !availability.getOrDefault("sunday", true)) {
                    return false;
                }
                if (worshipType == WorshipType.DAWN && !availability.getOrDefault("dawn", true)) {
                    return false;
                }
            }
        }

        // Specialty 확인
        Specialty s = member.getSpecialty();

        if (s == Specialty.ALL)
            return true;

        if (s == Specialty.WEEKDAY && worshipType == WorshipType.WEEKDAY)
            return true;
        if (s == Specialty.SUNDAY && (worshipType == WorshipType.SUNDAY_1 || worshipType == WorshipType.SUNDAY_2
                || worshipType == WorshipType.SUNDAY_3))
            return true;
        if (s == Specialty.DAWN && worshipType == WorshipType.DAWN)
            return true;

        return false;
    }
}
