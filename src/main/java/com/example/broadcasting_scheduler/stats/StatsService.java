package com.example.broadcasting_scheduler.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.broadcasting_scheduler.member.Member;
import com.example.broadcasting_scheduler.member.MemberRepository;
import com.example.broadcasting_scheduler.schedule.Schedule;
import com.example.broadcasting_scheduler.schedule.ScheduleRepository;

@Service
@Transactional
public class StatsService {

    private final MonthlyStatsRepository monthlyStatsRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public StatsService(MonthlyStatsRepository monthlyStatsRepository,
                       ScheduleRepository scheduleRepository,
                       MemberRepository memberRepository) {
        this.monthlyStatsRepository = monthlyStatsRepository;
        this.scheduleRepository = scheduleRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 특정 월의 통계를 계산하고 저장
     */
    public void calculateAndSaveMonthlyStats(int year, int month) {
        List<Schedule> schedules = scheduleRepository.findByDateBetween(
            java.time.LocalDate.of(year, month, 1),
            java.time.LocalDate.of(year, month, java.time.LocalDate.of(year, month, 1).lengthOfMonth())
        );

        Map<Member, Integer> countMap = new HashMap<>();
        for (Schedule schedule : schedules) {
            countMap.put(schedule.getMember(), countMap.getOrDefault(schedule.getMember(), 0) + 1);
        }

        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            int count = countMap.getOrDefault(member, 0);
            MonthlyStats stats = monthlyStatsRepository
                .findByYearAndMonthAndMember(year, month, member)
                .orElse(new MonthlyStats(year, month, member, count));
            stats.setCount(count);
            monthlyStatsRepository.save(stats);
        }
    }

    /**
     * 전월 통계를 가져와서 Member의 lastMonthCount를 업데이트
     */
    public void updateLastMonthCounts(int year, int month) {
        // 전월 계산
        int prevYear = year;
        int prevMonth = month - 1;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear--;
        }

        List<MonthlyStats> prevStats = monthlyStatsRepository.findByYearAndMonth(prevYear, prevMonth);
        Map<Member, Integer> countMap = new HashMap<>();

        for (MonthlyStats stat : prevStats) {
            countMap.put(stat.getMember(), stat.getCount());
        }

        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            member.setLastMonthCount(countMap.getOrDefault(member, 0));
            memberRepository.save(member);
        }
    }
}
