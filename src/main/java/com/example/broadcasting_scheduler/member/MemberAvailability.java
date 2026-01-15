package com.example.broadcasting_scheduler.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class MemberAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private Member member;
    
    private int dayOfWeek; // 1=월요일, 2=화요일, ..., 7=일요일
    private boolean weekdayEnabled; // 평일 예배 가능
    private boolean dawnEnabled; // 새벽 예배 가능
    private boolean sunday1Enabled; // 주일 1부 가능
    private boolean sunday2Enabled; // 주일 2부 가능
    private boolean sunday3Enabled; // 주일 3부 가능
    
    protected MemberAvailability() {
    }
    
    public MemberAvailability(Member member, int dayOfWeek, boolean weekdayEnabled, boolean dawnEnabled, 
                             boolean sunday1Enabled, boolean sunday2Enabled, boolean sunday3Enabled) {
        this.member = member;
        this.dayOfWeek = dayOfWeek;
        this.weekdayEnabled = weekdayEnabled;
        this.dawnEnabled = dawnEnabled;
        this.sunday1Enabled = sunday1Enabled;
        this.sunday2Enabled = sunday2Enabled;
        this.sunday3Enabled = sunday3Enabled;
    }
    
    public Long getId() {
        return id;
    }
    
    public Member getMember() {
        return member;
    }
    
    public void setMember(Member member) {
        this.member = member;
    }
    
    public int getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public boolean isWeekdayEnabled() {
        return weekdayEnabled;
    }
    
    public void setWeekdayEnabled(boolean weekdayEnabled) {
        this.weekdayEnabled = weekdayEnabled;
    }
    
    public boolean isDawnEnabled() {
        return dawnEnabled;
    }
    
    public void setDawnEnabled(boolean dawnEnabled) {
        this.dawnEnabled = dawnEnabled;
    }
    
    public boolean isSunday1Enabled() {
        return sunday1Enabled;
    }
    
    public void setSunday1Enabled(boolean sunday1Enabled) {
        this.sunday1Enabled = sunday1Enabled;
    }
    
    public boolean isSunday2Enabled() {
        return sunday2Enabled;
    }
    
    public void setSunday2Enabled(boolean sunday2Enabled) {
        this.sunday2Enabled = sunday2Enabled;
    }
    
    public boolean isSunday3Enabled() {
        return sunday3Enabled;
    }
    
    public void setSunday3Enabled(boolean sunday3Enabled) {
        this.sunday3Enabled = sunday3Enabled;
    }
}
