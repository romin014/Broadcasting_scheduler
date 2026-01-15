package com.example.broadcasting_scheduler.schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDateBetween(LocalDate start, LocalDate end);
}
