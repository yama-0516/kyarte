package com.example.kyarte.config;

import com.example.kyarte.entity.CalendarEvent;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.service.CalendarService;
import com.example.kyarte.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DemoDataInitializer implements ApplicationRunner {
	@Autowired
	private CalendarService calendarService;

	@Autowired
	private EmployeeService employeeService;

	@Override
	public void run(ApplicationArguments args) {
		try {
			List<CalendarEvent> events = calendarService.getAllEvents();
			LocalDateTime now = LocalDateTime.now();

			// 1) 既存の過去イベントを未来にシフト
			boolean shifted = false;
			for (CalendarEvent e : events) {
				if (e.getStartTime() != null && e.getStartTime().isBefore(now)) {
					// 元の時間帯は維持しつつ、次の週同曜日へシフト
					LocalDate targetDate = LocalDate.now().plusWeeks(1);
					LocalTime time = e.getStartTime().toLocalTime();
					LocalDateTime newStart = LocalDateTime.of(targetDate, time);
					LocalDateTime newEnd = e.getEndTime() != null ?
						newStart.plusMinutes(java.time.Duration.between(e.getStartTime(), e.getEndTime()).toMinutes())
						: newStart.plusHours(1);
					e.setStartTime(newStart);
					e.setEndTime(newEnd);
					calendarService.saveEvent(e);
					shifted = true;
				}
			}

			// 2) イベントが無い場合はデモを生成（常に未来日）
			if (events.isEmpty()) {
				List<Employee> employees = new ArrayList<>();
				try { employees = employeeService.getAllEmployees(); } catch (Exception ignored) {}
				if (employees.isEmpty()) return; // 従業員が居なければスキップ

				for (int i = 0; i < Math.min(10, employees.size()); i++) {
					Employee emp = employees.get(i);
					CalendarEvent ev = new CalendarEvent();
					ev.setTitle(emp.getLastName() + emp.getFirstName() + " デモ予定");
					ev.setDescription("自動生成されたデモ予定");
					ev.setEventType(i % 3 == 0 ? "meeting" : (i % 3 == 1 ? "deadline" : "other"));
					LocalDateTime start = LocalDateTime.now().plusDays(i + 1).withHour(10).withMinute(0);
					LocalDateTime end = start.plusHours(1);
					ev.setStartTime(start);
					ev.setEndTime(end);
					ev.setEmployee(emp);
					calendarService.saveEvent(ev);
				}
			}
		} catch (Exception e) {
			System.err.println("DemoDataInitializer error: " + e.getMessage());
		}
	}
}
