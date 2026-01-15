package com.example.broadcasting_scheduler.schedule;

import java.time.LocalDate;

import com.example.broadcasting_scheduler.member.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private WorshipType worshipType;

    @ManyToOne
    private Member member;

    protected Schedule() {
    }

    public Schedule(LocalDate date, WorshipType worshipType, Member member) {
        this.date = date;
        this.worshipType = worshipType;
        this.member = member;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public WorshipType getWorshipType() {
        return worshipType;
    }

    public Member getMember() {
        return member;
    }
}
