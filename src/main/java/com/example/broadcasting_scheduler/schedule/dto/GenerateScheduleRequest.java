package com.example.broadcasting_scheduler.schedule.dto;

import java.util.Map;

public class GenerateScheduleRequest {
    private int year;
    private int month;
    private Map<Long, Map<String, Boolean>> memberAvailability; // memberId -> {weekday: true/false, sunday: true/false, dawn: true/false}

    public GenerateScheduleRequest() {
    }

    public GenerateScheduleRequest(int year, int month, Map<Long, Map<String, Boolean>> memberAvailability) {
        this.year = year;
        this.month = month;
        this.memberAvailability = memberAvailability;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Map<Long, Map<String, Boolean>> getMemberAvailability() {
        return memberAvailability;
    }

    public void setMemberAvailability(Map<Long, Map<String, Boolean>> memberAvailability) {
        this.memberAvailability = memberAvailability;
    }
}
