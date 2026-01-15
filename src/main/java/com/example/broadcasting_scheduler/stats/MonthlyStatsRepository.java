package com.example.broadcasting_scheduler.stats;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.broadcasting_scheduler.member.Member;

public interface MonthlyStatsRepository extends JpaRepository<MonthlyStats, Long> {
    Optional<MonthlyStats> findByYearAndMonthAndMember(int year, int month, Member member);
    List<MonthlyStats> findByYearAndMonth(int year, int month);
}
