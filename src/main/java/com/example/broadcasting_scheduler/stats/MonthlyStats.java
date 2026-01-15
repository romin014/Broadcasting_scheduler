package com.example.broadcasting_scheduler.stats;

import com.example.broadcasting_scheduler.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class MonthlyStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_year")
    private int year;
    
    @Column(name = "stat_month")
    private int month;

    @ManyToOne
    private Member member;

    private int count;

    protected MonthlyStats() {
    }

    public MonthlyStats(int year, int month, Member member, int count) {
        this.year = year;
        this.month = month;
        this.member = member;
        this.count = count;
    }

    public Long getId() {
        return id;
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

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
