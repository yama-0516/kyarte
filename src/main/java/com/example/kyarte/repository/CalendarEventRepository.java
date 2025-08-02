package com.example.kyarte.repository;

import com.example.kyarte.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    // 従業員IDでイベントを検索
    List<CalendarEvent> findByEmployeeIdOrderByStartTimeAsc(Long employeeId);
    
    // 期間内のイベントを検索
    @Query("SELECT e FROM CalendarEvent e WHERE e.startTime BETWEEN :startDate AND :endDate ORDER BY e.startTime ASC")
    List<CalendarEvent> findEventsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    // 従業員IDと期間でイベントを検索
    @Query("SELECT e FROM CalendarEvent e WHERE e.employee.id = :employeeId AND e.startTime BETWEEN :startDate AND :endDate ORDER BY e.startTime ASC")
    List<CalendarEvent> findEventsByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
    
    // イベントタイプで検索
    List<CalendarEvent> findByEventTypeOrderByStartTimeAsc(String eventType);
    
    // 同期されていないイベントを検索
    List<CalendarEvent> findByIsSyncedFalse();
    
    // Google Calendar IDで検索
    CalendarEvent findByGoogleCalendarId(String googleCalendarId);
    
    // 今日のイベントを検索
    @Query("SELECT e FROM CalendarEvent e WHERE e.startTime BETWEEN :start AND :end ORDER BY e.startTime ASC")
    List<CalendarEvent> findTodayEvents(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // 今週のイベントを検索
    @Query("SELECT e FROM CalendarEvent e WHERE e.startTime BETWEEN :weekStart AND :weekEnd ORDER BY e.startTime ASC")
    List<CalendarEvent> findThisWeekEvents(@Param("weekStart") LocalDateTime weekStart,
                                          @Param("weekEnd") LocalDateTime weekEnd);

    @Query("SELECT e FROM CalendarEvent e WHERE e.startTime BETWEEN :start AND :end ORDER BY e.startTime ASC")
    List<CalendarEvent> findEventsByDateRange(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
    
    // 直近のイベントを取得（ホームページ用）
    @Query(value = "SELECT * FROM calendar_events WHERE start_time >= :now ORDER BY start_time ASC LIMIT :limit", nativeQuery = true)
    List<CalendarEvent> findUpcomingEvents(@Param("now") LocalDateTime now, @Param("limit") int limit);
}