package com.example.broadcasting_scheduler.schedule.dto;

import java.time.LocalDate;

import com.example.broadcasting_scheduler.schedule.Schedule;
import com.example.broadcasting_scheduler.schedule.WorshipType;

public class ScheduleResponseDto {
    private Long id;
    private LocalDate date;
    private WorshipType worshipType;
    private String memberName;
    private Long memberId;

    public ScheduleResponseDto() {
    }

    public ScheduleResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.date = schedule.getDate();
        this.worshipType = schedule.getWorshipType();
        this.memberName = schedule.getMember().getName();
        this.memberId = schedule.getMember().getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public WorshipType getWorshipType() {
        return worshipType;
    }

    public void setWorshipType(WorshipType worshipType) {
        this.worshipType = worshipType;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
