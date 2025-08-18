package com.example.kyarte.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calendar_events")
public class CalendarEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "location")
    private String location;
    
    // 参加者（カンマ区切り）
    @Column(name = "attendees")
    private String attendees;
    
    @Column(name = "event_type")
    private String eventType; // meeting, vacation, deadline, etc.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Column(name = "google_calendar_id")
    private String googleCalendarId;
    
    @Column(name = "is_synced")
    private boolean isSynced = false;
    
    // 非公開フラグ
    @Column(name = "is_private")
    private boolean isPrivate = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // コンストラクタ
    public CalendarEvent() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getAttendees() { return attendees; }
    public void setAttendees(String attendees) { this.attendees = attendees; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public String getGoogleCalendarId() { return googleCalendarId; }
    public void setGoogleCalendarId(String googleCalendarId) { this.googleCalendarId = googleCalendarId; }
    
    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
    
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 