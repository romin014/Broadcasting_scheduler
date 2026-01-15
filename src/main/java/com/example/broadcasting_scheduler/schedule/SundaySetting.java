package com.example.broadcasting_scheduler.schedule;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class SundaySetting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private boolean sunday1Enabled; // 주일 1부 활성화
    private boolean sunday2Enabled; // 주일 2부 활성화
    private boolean sunday3Enabled; // 주일 3부 활성화
    
    protected SundaySetting() {
    }
    
    public SundaySetting(boolean sunday1Enabled, boolean sunday2Enabled, boolean sunday3Enabled) {
        this.sunday1Enabled = sunday1Enabled;
        this.sunday2Enabled = sunday2Enabled;
        this.sunday3Enabled = sunday3Enabled;
    }
    
    public Long getId() {
        return id;
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
