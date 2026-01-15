package com.example.broadcasting_scheduler.member.dto;

import com.example.broadcasting_scheduler.member.Member;
import com.example.broadcasting_scheduler.member.Specialty;

public class MemberResponseDto {
    private Long id;
    private String name;
    private Specialty specialty;
    private int lastMonthCount;

    public MemberResponseDto() {
    }

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.specialty = member.getSpecialty();
        this.lastMonthCount = member.getLastMonthCount();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getLastMonthCount() {
        return lastMonthCount;
    }

    public void setLastMonthCount(int lastMonthCount) {
        this.lastMonthCount = lastMonthCount;
    }
}
