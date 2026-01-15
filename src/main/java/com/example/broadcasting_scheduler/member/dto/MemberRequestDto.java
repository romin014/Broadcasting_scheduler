package com.example.broadcasting_scheduler.member.dto;

import com.example.broadcasting_scheduler.member.Specialty;

public class MemberRequestDto {
    private String name;
    private Specialty specialty;
    private boolean canServeWeekday;
    private boolean canServeSunday;
    private boolean canServeDawn;

    public MemberRequestDto() {
    }

    public MemberRequestDto(String name, Specialty specialty, boolean canServeWeekday, boolean canServeSunday, boolean canServeDawn) {
        this.name = name;
        this.specialty = specialty;
        this.canServeWeekday = canServeWeekday;
        this.canServeSunday = canServeSunday;
        this.canServeDawn = canServeDawn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public boolean isCanServeWeekday() {
        return canServeWeekday;
    }

    public void setCanServeWeekday(boolean canServeWeekday) {
        this.canServeWeekday = canServeWeekday;
    }

    public boolean isCanServeSunday() {
        return canServeSunday;
    }

    public void setCanServeSunday(boolean canServeSunday) {
        this.canServeSunday = canServeSunday;
    }

    public boolean isCanServeDawn() {
        return canServeDawn;
    }

    public void setCanServeDawn(boolean canServeDawn) {
        this.canServeDawn = canServeDawn;
    }
}
