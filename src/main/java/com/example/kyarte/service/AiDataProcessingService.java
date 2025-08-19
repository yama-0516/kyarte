package com.example.kyarte.service;

import com.example.kyarte.dto.AiAnalysisResult;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.entity.FreeNote;
import com.example.kyarte.entity.CalendarEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
@Transactional
public class AiDataProcessingService {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private FreeNoteService freeNoteService;
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    @Autowired
    private CalendarService calendarService;
    
    /**
     * FreeNoteをAI解析して従業員データに反映
     */
    public void processFreeNoteWithAi(FreeNote freeNote) {
        try {
            // 複数の情報を全て解析
            List<AiAnalysisResult> analysisResults = aiAnalysisService.analyzeMultipleContent(freeNote.getContent());
            
            System.out.println("=== AiDataProcessingService Debug ===");
            System.out.println("Processing " + analysisResults.size() + " analysis results");
            
            // 各解析結果を従業員データに反映
            for (AiAnalysisResult analysis : analysisResults) {
                System.out.println("Processing result for: " + analysis.getEmployeeName());
                processAnalysisResult(analysis, freeNote);
            }
            
            // FreeNoteを処理済みにマーク
            freeNote.setProcessed(true);
            freeNote.setProcessedAt(LocalDateTime.now());
            freeNoteService.saveFreeNote(freeNote);
            
        } catch (Exception e) {
            // エラー時は未分類としてマーク
            freeNote.setProcessed(true);
            freeNote.setProcessedAt(LocalDateTime.now());
            freeNoteService.saveFreeNote(freeNote);
            
            // エラーログ出力（本格運用時はログライブラリを使用）
            System.err.println("AI解析エラー: " + e.getMessage());
        }
    }
    
    /**
     * AI解析結果を従業員データに反映
     */
    private void processAnalysisResult(AiAnalysisResult analysis, FreeNote originalNote) {
        String employeeName = analysis.getEmployeeName();
        
        System.out.println("=== AiDataProcessingService Debug ===");
        System.out.println("Processing analysis result for employee: " + employeeName);
        
        if (employeeName == null || employeeName.isEmpty()) {
            // 従業員名が見つからない場合でも、予定カテゴリなら汎用イベントとして登録
            if (AiAnalysisResult.CATEGORY_SCHEDULE.equals(analysis.getCategory())) {
                System.out.println("Employee name missing but category is schedule; creating general calendar event");
                createGeneralScheduleEvent(analysis);
            } else {
                System.out.println("Employee name is null or empty, skipping processing");
            }
            return;
        }
        
        // 既存の従業員を検索
        List<Employee> employees = employeeService.searchEmployeesByName(employeeName);
        System.out.println("Found " + employees.size() + " employees matching name: " + employeeName);
        
        for (Employee emp : employees) {
            System.out.println("  - Employee: " + emp.getFullName() + " (ID: " + emp.getId() + ")");
        }
        
        Employee targetEmployee = null;
        
        if (!employees.isEmpty()) {
            // 名前が一致する従業員が見つかった場合
            targetEmployee = employees.get(0);
            System.out.println("Using existing employee: " + targetEmployee.getFullName());
        } else {
            // 見つからない場合は新規作成
            System.out.println("No matching employee found, creating new one");
            targetEmployee = createNewEmployee(employeeName);
        }
        
        // 解析結果に基づいて従業員データを更新
        System.out.println("Updating employee data...");
        updateEmployeeData(targetEmployee, analysis, originalNote);
        System.out.println("Employee data update completed");
    }
    
    /**
     * 新しい従業員を作成
     */
    private Employee createNewEmployee(String employeeName) {
        Employee newEmployee = new Employee();
        newEmployee.setLastName(employeeName);
        newEmployee.setFirstName(""); // 名は空で作成
        newEmployee.setNotes("AI解析により自動作成された従業員データ");
        
        return employeeService.saveEmployee(newEmployee);
    }
    
    /**
     * 従業員データを更新
     */
    private void updateEmployeeData(Employee employee, AiAnalysisResult analysis, FreeNote originalNote) {
        String currentNotes = employee.getNotes() != null ? employee.getNotes() : "";
        String newNote = createFormattedNote(analysis, originalNote);
        
        // 既存の備考に新しい情報を追加
        String updatedNotes = currentNotes.isEmpty() ? newNote : currentNotes + "\n\n" + newNote;
        employee.setNotes(updatedNotes);
        
        // カテゴリに応じた追加処理
        switch (analysis.getCategory()) {
            case AiAnalysisResult.CATEGORY_VACATION:
                // 有給情報の処理
                processVacationInfo(employee, analysis);
                break;
            case AiAnalysisResult.CATEGORY_HEALTH:
                // 健康情報の処理
                processHealthInfo(employee, analysis);
                break;
            case AiAnalysisResult.CATEGORY_SCHEDULE:
                // 予定情報の処理
                processScheduleInfo(employee, analysis);
                break;
            case AiAnalysisResult.CATEGORY_PERSONAL:
                // 個人情報（誕生日など）の処理
                processPersonalInfo(employee, analysis);
                break;
            case AiAnalysisResult.CATEGORY_PERFORMANCE:
                // 評価情報の処理
                processPerformanceInfo(employee, analysis);
                break;
            default:
                // その他の情報は備考欄に追加
                break;
        }
        
        employeeService.saveEmployee(employee);
    }
    
    /**
     * フォーマットされた備考を作成
     */
    private String createFormattedNote(AiAnalysisResult analysis, FreeNote originalNote) {
        StringBuilder note = new StringBuilder();
        note.append("【").append(analysis.getCategory()).append("】");
        note.append(" ").append(analysis.getContent());
        note.append(" (AI解析: ").append(aiAnalysisService.getServiceName()).append(")");
        note.append(" - ").append(originalNote.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        
        if ("low".equals(analysis.getConfidence())) {
            note.append(" [信頼度: 低]");
        }
        
        return note.toString();
    }
    
    /**
     * 有給情報の処理
     */
    private void processVacationInfo(Employee employee, AiAnalysisResult analysis) {
        // 有給関連の特別処理（例：有給日数の管理など）
        // 将来的に有給管理テーブルを作成して連携
    }
    
    /**
     * 健康情報の処理
     */
    private void processHealthInfo(Employee employee, AiAnalysisResult analysis) {
        // 健康関連の特別処理
        // 将来的に健康管理テーブルを作成して連携
    }
    
    /**
     * 予定情報の処理 - AI解析結果からカレンダーイベントを作成
     */
    private void processScheduleInfo(Employee employee, AiAnalysisResult analysis) {
        System.out.println("\n=== processScheduleInfo Debug START ===");
        System.out.println("Employee: " + (employee != null ? employee.getFullName() + " (ID: " + employee.getId() + ")" : "NULL"));
        System.out.println("Analysis content: " + analysis.getContent());
        
        try {
            // Step 1: CalendarEventオブジェクト作成
            System.out.println("Step 1: Creating CalendarEvent object...");
            CalendarEvent event = new CalendarEvent();
            System.out.println("Step 1: SUCCESS - CalendarEvent object created");
            
            // Step 2: 基本情報設定
            System.out.println("Step 2: Setting basic event information...");
            String title = extractEventTitle(analysis.getContent());
            String eventType = determineEventType(analysis.getContent());
            
            event.setTitle(title);
            event.setDescription(analysis.getContent());
            event.setEventType(eventType);
            event.setLocation(extractEventLocation(analysis.getContent()));
            String attendees = extractEventAttendees(analysis.getContent());
            attendees = ensureIncludesEmployee(attendees, employee);
            event.setAttendees(attendees);
            event.setPrivate(extractPrivacy(analysis.getContent()));
            
            System.out.println("Step 2: SUCCESS - Title: " + title + ", Type: " + eventType);
            
            // Step 3: 日時設定
            System.out.println("Step 3: Setting event date/time...");
            LocalDateTime eventDateTime = extractEventDateTime(analysis.getContent());
            LocalDateTime endDateTime = eventDateTime.plusHours(1);
            
            event.setStartTime(eventDateTime);
            event.setEndTime(endDateTime);
            
            System.out.println("Step 3: SUCCESS - Start: " + eventDateTime + ", End: " + endDateTime);
            
            // Step 4: 従業員情報設定
            System.out.println("Step 4: Setting employee information...");
            if (employee == null) {
                throw new RuntimeException("Employee is null - cannot create calendar event");
            }
            event.setEmployee(employee);
            System.out.println("Step 4: SUCCESS - Employee set: " + employee.getFullName());
            
            // Step 5: データベース保存
            System.out.println("Step 5: Saving calendar event to database...");
            CalendarEvent savedEvent = calendarService.saveEvent(event);
            System.out.println("Step 5: SUCCESS - Calendar event saved with ID: " + savedEvent.getId());
            
            System.out.println("=== processScheduleInfo Debug END (SUCCESS) ===");
            
        } catch (Exception e) {
            System.err.println("\n=== processScheduleInfo ERROR ===");
            System.err.println("Error Type: " + e.getClass().getSimpleName());
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("Employee Info: " + (employee != null ? employee.getFullName() + " (ID: " + employee.getId() + ")" : "NULL"));
            System.err.println("Analysis Content: " + analysis.getContent());
            e.printStackTrace();
            System.err.println("=== processScheduleInfo Debug END (ERROR) ===");
            
            // エラーを再スローして上位でキャッチできるようにする
            throw new RuntimeException("Calendar event creation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * AI解析結果からイベントタイトルを抽出
     */
    private String extractEventTitle(String content) {
        // 簡単なルールベースでタイトルを生成
        if (content.contains("会議")) {
            return "会議";
        } else if (content.contains("有給") || content.contains("休暇")) {
            return "有給休暇";
        } else if (content.contains("締切") || content.contains("期限")) {
            return "締切";
        } else if (content.contains("予定")) {
            return "予定";
        } else {
            return "その他の予定";
        }
    }
    
    /**
     * AI解析結果からイベントタイプを決定
     */
    private String determineEventType(String content) {
        if (content.contains("会議")) {
            return "meeting";
        } else if (content.contains("有給") || content.contains("休暇")) {
            return "vacation";
        } else if (content.contains("締切") || content.contains("期限")) {
            return "deadline";
        } else {
            return "other";
        }
    }
    
    /**
     * AI解析結果から日時を抽出（デフォルトは明日の9時）
     */
    private LocalDateTime extractEventDateTime(String content) {
        LocalDateTime defaultDateTime = LocalDate.now().plusDays(1).atTime(9, 0);
        
        // 簡単なルールベースで日時を抽出
        if (content.contains("今日")) {
            return LocalDate.now().atTime(9, 0);
        } else if (content.contains("明日")) {
            return LocalDate.now().plusDays(1).atTime(9, 0);
        } else if (content.contains("来週")) {
            return LocalDate.now().plusWeeks(1).atTime(9, 0);
        } else {
            return defaultDateTime;
        }
    }
    
    /**
     * 評価情報の処理
     */
    private void processPerformanceInfo(Employee employee, AiAnalysisResult analysis) {
        // 評価関連の特別処理
        // 将来的に評価管理テーブルを作成して連携
    }
    
    /**
     * 未処理のFreeNoteを一括処理
     */
    public void processAllUnprocessedNotes() {
        List<FreeNote> unprocessedNotes = freeNoteService.getUnprocessedNotes();
        
        for (FreeNote note : unprocessedNotes) {
            processFreeNoteWithAi(note);
        }
    }
    
    /**
     * 個人情報（誕生日など）の処理
     */
    private void processPersonalInfo(Employee employee, AiAnalysisResult analysis) {
        String content = analysis.getContent();
        if (content == null) {
            return;
        }
        String lc = content.toLowerCase();
        boolean isBirthday = content.contains("誕生日") || content.contains("バースデー") || lc.contains("birthday");
        if (!isBirthday) {
            return;
        }
        try {
            CalendarEvent event = new CalendarEvent();
            event.setTitle("誕生日");
            event.setDescription(content);
            event.setEventType("birthday");
            event.setLocation(extractEventLocation(content));
            String attendees = extractEventAttendees(content);
            attendees = ensureIncludesEmployee(attendees, employee);
            event.setAttendees(attendees);
            event.setPrivate(extractPrivacy(content));
            event.setLocation("");
            LocalDateTime start = extractEventDateTime(content);
            LocalDateTime end = start.plusHours(1);
            event.setStartTime(start);
            event.setEndTime(end);
            if (employee == null) {
                throw new RuntimeException("Employee is null - cannot create birthday event");
            }
            event.setEmployee(employee);
            calendarService.saveEvent(event);
        } catch (Exception e) {
            System.err.println("processPersonalInfo error: " + e.getMessage());
        }
    }

	private void createGeneralScheduleEvent(AiAnalysisResult analysis) {
		System.out.println("\n=== createGeneralScheduleEvent Debug START ===");
		try {
			CalendarEvent event = new CalendarEvent();
			event.setTitle(extractEventTitle(analysis.getContent()));
			event.setDescription(analysis.getContent());
			event.setEventType(determineEventType(analysis.getContent()));
			event.setLocation(extractEventLocation(analysis.getContent()));
			event.setAttendees(extractEventAttendees(analysis.getContent()));
			event.setPrivate(extractPrivacy(analysis.getContent()));
			java.time.LocalDateTime start = extractEventDateTime(analysis.getContent());
			java.time.LocalDateTime end = start.plusHours(1);
			event.setStartTime(start);
			event.setEndTime(end);
			// 従業員は紐付けない（NULL）
			calendarService.saveEvent(event);
			System.out.println("=== createGeneralScheduleEvent Debug END (SUCCESS) ===");
		} catch (Exception e) {
			System.err.println("createGeneralScheduleEvent ERROR: " + e.getMessage());
		}
	}
	
	private String extractEventLocation(String content) {
		// 例: 「@渋谷オフィス」「場所:会議室A」
		java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("@([^\n\r\s]+)").matcher(content);
		if (m1.find()) return m1.group(1);
		java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("場所\s*[:：]\s*([^\n\r]+)").matcher(content);
		if (m2.find()) return m2.group(1).trim();
		return "";
	}
	
	private String extractEventAttendees(String content) {
		// 1) 明示指定のパターンを優先: 「参加者: 田中, 佐藤」「with 田中・佐藤」
		java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("参加者\s*[:：]\s*([^\n\r]+)").matcher(content);
		if (m1.find()) return m1.group(1).replaceAll("[・、]", ",").replaceAll("\s+", "").trim();
		java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("with\s+([^\n\r]+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(content);
		if (m2.find()) return m2.group(1).replaceAll("[・、]", ",").replaceAll("\s+", "").trim();
		
		// 2) フォールバック: 文中から従業員名（姓/名）を抽出してリスト化
		try {
			List<Employee> all = employeeService.getAllEmployees();
			Set<String> detected = new LinkedHashSet<>();
			for (Employee e : all) {
				String last = e.getLastName() != null ? e.getLastName().trim() : "";
				String first = e.getFirstName() != null ? e.getFirstName().trim() : "";
				if (!last.isEmpty() && content.contains(last)) {
					detected.add(last);
				}
				if (!first.isEmpty() && content.contains(first)) {
					// 名でヒットした場合も姓で登録
					detected.add(last.isEmpty() ? first : last);
				}
			}
			if (!detected.isEmpty()) {
				return String.join(",", detected);
			}
		} catch (Exception ignore) {}
		
		return "";
	}
	
	private boolean extractPrivacy(String content) {
		String lc = content.toLowerCase();
		if (content.contains("非公開") || lc.contains("private") || lc.contains("confidential")) return true;
		if (content.contains("公開") || lc.contains("public")) return false;
		return false;
	}
	
	// 参加者文字列にメイン従業員（姓）を含める
	private String ensureIncludesEmployee(String attendees, Employee employee) {
		if (employee == null) return attendees != null ? attendees : "";
		Set<String> set = new LinkedHashSet<>();
		if (attendees != null && !attendees.trim().isEmpty()) {
			for (String part : attendees.split(",")) {
				String p = part.trim();
				if (!p.isEmpty()) set.add(p);
			}
		}
		String mainName = employee.getLastName() != null ? employee.getLastName().trim() : "";
		if (!mainName.isEmpty()) set.add(mainName);
		return String.join(",", set);
	}
} 