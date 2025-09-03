package com.example.kyarte.controller;

import com.example.kyarte.entity.CalendarEvent;
import com.example.kyarte.service.CalendarService;
import com.example.kyarte.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
public class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @MockBean
    private EmployeeService employeeService;

    @Test
    public void testTodayEvents() throws Exception {
        Mockito.when(calendarService.getTodayEventsByTimeRange())
               .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/calendar/today"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("events"))
               .andExpect(model().attribute("title", "今日のイベント"))
               .andExpect(view().name("calendar/list"));
    }

    @Test
    public void testWeekEvents() throws Exception {
        Mockito.when(calendarService.getThisWeekEvents())
               .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/calendar/week"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("events"))
               .andExpect(model().attribute("title", "今週のイベント"))
               .andExpect(view().name("calendar/list"));
    }
}