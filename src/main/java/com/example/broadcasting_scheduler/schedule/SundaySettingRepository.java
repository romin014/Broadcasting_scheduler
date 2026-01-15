package com.example.broadcasting_scheduler.schedule;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SundaySettingRepository extends JpaRepository<SundaySetting, Long> {
    Optional<SundaySetting> findFirstByOrderByIdAsc();
}
