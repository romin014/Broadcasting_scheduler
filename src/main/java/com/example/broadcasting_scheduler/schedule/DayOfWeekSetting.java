package com.example.broadcasting_scheduler.schedule;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DayOfWeekSetting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private int dayOfWeek; // 1=월요일, 2=화요일, ..., 7=일요일
    private boolean weekdayEnabled; // 평일 예배 활성화
    private boolean dawnEnabled; // 새벽 예배 활성화
    
    protected DayOfWeekSetting() {
    }
    
    public DayOfWeekSetting(int dayOfWeek, boolean weekdayEnabled, boolean dawnEnabled) {
        this.dayOfWeek = dayOfWeek;
        this.weekdayEnabled = weekdayEnabled;
        this.dawnEnabled = dawnEnabled;
    }
    
    public Long getId() {
        return id;
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
}
