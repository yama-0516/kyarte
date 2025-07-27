package com.example.kyarte.service;

import com.example.kyarte.dto.AiAnalysisResult;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.entity.FreeNote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AiDataProcessingService {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private FreeNoteService freeNoteService;
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
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
            // 従業員名が見つからない場合は未分類として処理
            System.out.println("Employee name is null or empty, skipping processing");
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
     * 予定情報の処理
     */
    private void processScheduleInfo(Employee employee, AiAnalysisResult analysis) {
        // 予定関連の特別処理
        // 将来的に予定管理テーブルを作成して連携
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
} 