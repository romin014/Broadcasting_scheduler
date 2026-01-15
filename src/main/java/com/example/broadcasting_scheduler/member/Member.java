package com.example.broadcasting_scheduler.member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Specialty specialty;

    /**
     * 전월 배정 횟수 (스케줄 생성 시 참고용)
     * 실제로는 통계 테이블로 분리해도 됨
     */
    private int lastMonthCount;

    protected Member() {
        // JPA 기본 생성자
    }

    public Member(String name, Specialty specialty, int lastMonthCount) {
        this.name = name;
        this.specialty = specialty;
        this.lastMonthCount = lastMonthCount;
    }

    // ===== getter =====

    public Long getId() {
        return id;
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
