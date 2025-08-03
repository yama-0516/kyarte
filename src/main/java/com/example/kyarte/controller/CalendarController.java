package com.example.kyarte.controller;

import com.example.kyarte.entity.CalendarEvent;
import com.example.kyarte.service.CalendarService;
import com.example.kyarte.service.EmployeeService;
import com.example.kyarte.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/calendar")
public class CalendarController {
    
    @Autowired
    private CalendarService calendarService;
    
    @Autowired
    private EmployeeService employeeService;
    
    // カレンダー一覧表示
    @GetMapping
    public String calendar(Model model) {
        try {
            List<CalendarEvent> todayEvents = calendarService.getTodayEventsByTimeRange();
            List<CalendarEvent> weekEvents = calendarService.getThisWeekEvents();
            List<CalendarEvent> allEvents = calendarService.getAllEvents();
            
            model.addAttribute("todayEvents", todayEvents);
            model.addAttribute("weekEvents", weekEvents);
            model.addAttribute("allEvents", allEvents);
            model.addAttribute("today", LocalDate.now());
            
        } catch (Exception e) {
            model.addAttribute("error", "カレンダーデータの取得に失敗しました: " + e.getMessage());
        }
        return "calendar/index";
    }
    
    // イベント詳細表示
    @GetMapping("/{id}")
    public String showEvent(@PathVariable Long id, Model model) {
        try {
            CalendarEvent event = calendarService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("イベントが見つかりません"));
            
            model.addAttribute("event", event);
            return "calendar/show";
            
        } catch (Exception e) {
            model.addAttribute("error", "イベントの取得に失敗しました: " + e.getMessage());
            return "redirect:/calendar";
        }
    }
    
    // 新規イベント作成フォーム
    @GetMapping("/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new CalendarEvent());
        return "calendar/form";
    }
    
    // イベント作成
    @PostMapping
    public String createEvent(@ModelAttribute CalendarEvent event,
                            @RequestParam String startDate,
                            @RequestParam String startTime,
                            @RequestParam String endDate,
                            @RequestParam String endTime) {
        try {
            // 日時データを正しく設定
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T" + startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T" + endTime);
            
            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);
            
            // デフォルト従業員を設定（最初の従業員または新規作成）
            Employee defaultEmployee = getDefaultEmployee();
            event.setEmployee(defaultEmployee);
            
            calendarService.saveEvent(event);
            return "redirect:/calendar";
        } catch (Exception e) {
            System.err.println("Calendar save error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/calendar?error=" + e.getMessage();
        }
    }
    
    // イベント編集フォーム
    @GetMapping("/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        try {
            CalendarEvent event = calendarService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("イベントが見つかりません"));
            
            model.addAttribute("event", event);
            return "calendar/form";
            
        } catch (Exception e) {
            return "redirect:/calendar?error=" + e.getMessage();
        }
    }
    
    // イベント更新
    @PostMapping("/{id}")
    public String updateEvent(@PathVariable Long id, 
                            @ModelAttribute CalendarEvent event,
                            @RequestParam String startDate,
                            @RequestParam String startTime,
                            @RequestParam String endDate,
                            @RequestParam String endTime) {
        try {
            // 既存のイベントを取得して従業員情報を保持
            CalendarEvent existingEvent = calendarService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("イベントが見つかりません"));
            
            // 日時データを正しく設定
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T" + startTime);
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T" + endTime);
            
            event.setId(id);
            event.setStartTime(startDateTime);
            event.setEndTime(endDateTime);
            event.setEmployee(existingEvent.getEmployee()); // 既存の従業員情報を保持
            
            calendarService.saveEvent(event);
            return "redirect:/calendar";
        } catch (Exception e) {
            System.err.println("Calendar update error: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/calendar?error=" + e.getMessage();
        }
    }
    
    // イベント削除
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        try {
            calendarService.deleteEvent(id);
            return "redirect:/calendar";
        } catch (Exception e) {
            return "redirect:/calendar?error=" + e.getMessage();
        }
    }
    
    // 今日のイベント
    @GetMapping("/today")
    public String todayEvents(Model model) {
        try {
            List<CalendarEvent> events = calendarService.getTodayEventsByTimeRange();
            model.addAttribute("events", events);
            model.addAttribute("title", "今日のイベント");
            return "calendar/list";
        } catch (Exception e) {
            model.addAttribute("error", "データの取得に失敗しました: " + e.getMessage());
            return "calendar/list";
        }
    }
    
    // 今週のイベント
    @GetMapping("/week")
    public String weekEvents(Model model) {
        try {
            List<CalendarEvent> events = calendarService.getThisWeekEvents();
            model.addAttribute("events", events);
            model.addAttribute("title", "今週のイベント");
            return "calendar/list";
        } catch (Exception e) {
            model.addAttribute("error", "データの取得に失敗しました: " + e.getMessage());
            return "calendar/list";
        }
    }
    
    // イベントタイプ別表示
    @GetMapping("/type/{eventType}")
    public String eventsByType(@PathVariable String eventType, Model model) {
        try {
            List<CalendarEvent> events = calendarService.getEventsByType(eventType);
            model.addAttribute("events", events);
            model.addAttribute("title", eventType + "のイベント");
            return "calendar/list";
        } catch (Exception e) {
            model.addAttribute("error", "データの取得に失敗しました: " + e.getMessage());
            return "calendar/list";
        }
    }
    
    /**
     * デフォルト従業員を取得（最初の従業員または新規作成）
     */
    private Employee getDefaultEmployee() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            if (!employees.isEmpty()) {
                return employees.get(0); // 最初の従業員を返す
            } else {
                // 従業員が存在しない場合は新規作成
                Employee defaultEmployee = new Employee();
                defaultEmployee.setLastName("システム");
                defaultEmployee.setFirstName("管理者");
                defaultEmployee.setDepartment("管理部");
                defaultEmployee.setPosition("システム管理者");
                defaultEmployee.setEmail("system@company.com");
                defaultEmployee.setNotes("カレンダーイベント用のデフォルト従業員");
                return employeeService.saveEmployee(defaultEmployee);
            }
        } catch (Exception e) {
            System.err.println("Default employee creation error: " + e.getMessage());
            // エラー時は最小限の従業員オブジェクトを作成
            Employee fallbackEmployee = new Employee();
            fallbackEmployee.setLastName("システム");
            fallbackEmployee.setFirstName("ユーザー");
            return fallbackEmployee;
        }
    }
} 