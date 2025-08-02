package com.example.kyarte.service;

import com.example.kyarte.entity.CalendarEvent;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.repository.CalendarEventRepository;
import com.example.kyarte.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarService {
    
    @Autowired
    private CalendarEventRepository calendarEventRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // イベント保存
    public CalendarEvent saveEvent(CalendarEvent event) {
        return calendarEventRepository.save(event);
    }
    
    // イベント取得（ID）
    public Optional<CalendarEvent> getEventById(Long id) {
        return calendarEventRepository.findById(id);
    }
    
    // 全イベント取得
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }
    
    // 従業員のイベント取得
    public List<CalendarEvent> getEventsByEmployee(Long employeeId) {
        return calendarEventRepository.findByEmployeeIdOrderByStartTimeAsc(employeeId);
    }
    
    // 今日のイベント取得
    public List<CalendarEvent> getTodayEvents() {
        return calendarEventRepository.findTodayEvents();
    }
    
    // 今週のイベント取得
    public List<CalendarEvent> getThisWeekEvents() {
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.of(LocalDate.now().plusDays(7), LocalTime.MAX);
        return calendarEventRepository.findThisWeekEvents(weekStart, weekEnd);
    }
    
    // 期間内のイベント取得
    public List<CalendarEvent> getEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return calendarEventRepository.findEventsBetweenDates(startDate, endDate);
    }
    
    // イベントタイプで取得
    public List<CalendarEvent> getTodayEvents() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return calendarEventRepository.findTodayEvents(start, end);
    }
    
    
    // イベント削除
    public void deleteEvent(Long id) {
        calendarEventRepository.deleteById(id);
    }
    
    // AI解析結果からイベント作成
    public CalendarEvent createEventFromAiAnalysis(String content, String employeeName, String eventType) {
        CalendarEvent event = new CalendarEvent();
        
        // 従業員を検索
        List<Employee> employees = employeeRepository.findByLastNameContainingOrFirstNameContaining(employeeName, employeeName);
        if (!employees.isEmpty()) {
            event.setEmployee(employees.get(0));
        }
        
        // イベントタイプに基づいてタイトル設定
        switch (eventType.toLowerCase()) {
            case "vacation":
            case "有給":
                event.setTitle(employeeName + " 有給申請");
                event.setEventType("vacation");
                break;
            case "meeting":
            case "会議":
                event.setTitle(employeeName + " 会議");
                event.setEventType("meeting");
                break;
            case "deadline":
            case "締切":
                event.setTitle(employeeName + " 締切");
                event.setEventType("deadline");
                break;
            default:
                event.setTitle(employeeName + " " + eventType);
                event.setEventType("other");
        }
        
        event.setDescription(content);
        event.setStartTime(LocalDateTime.now().plusDays(1)); // デフォルト：明日
        event.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1)); // デフォルト：1時間後
        
        return saveEvent(event);
    }
    
    // 同期されていないイベント取得
    public List<CalendarEvent> getUnsyncedEvents() {
        return calendarEventRepository.findByIsSyncedFalse();
    }
    
    // イベントを同期済みにマーク
    public void markEventAsSynced(Long eventId, String googleCalendarId) {
        Optional<CalendarEvent> eventOpt = getEventById(eventId);
        if (eventOpt.isPresent()) {
            CalendarEvent event = eventOpt.get();
            event.setSynced(true);
            event.setGoogleCalendarId(googleCalendarId);
            saveEvent(event);
        }
    }
} 