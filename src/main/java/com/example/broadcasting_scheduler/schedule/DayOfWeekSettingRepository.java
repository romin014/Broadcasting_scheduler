package com.example.broadcasting_scheduler.schedule;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DayOfWeekSettingRepository extends JpaRepository<DayOfWeekSetting, Long> {
    Optional<DayOfWeekSetting> findByDayOfWeek(int dayOfWeek);
}
