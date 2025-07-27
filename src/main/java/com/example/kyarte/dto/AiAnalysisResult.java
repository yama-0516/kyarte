package com.example.kyarte.dto;

public class AiAnalysisResult {
    
    private String employeeName;        // 従業員名
    private String action;              // アクション（add_note, update_info, schedule_event等）
    private String content;             // 内容
    private String category;            // カテゴリ（vacation, health, schedule, performance, personal, uncategorized）
    private String confidence;          // 信頼度（high, medium, low）
    private String rawResponse;         // AIの生の応答（デバッグ用）
    
    // デフォルトコンストラクタ
    public AiAnalysisResult() {}
    
    // 手動でコンストラクタを追加（Lombokのバックアップ）
    public AiAnalysisResult(String employeeName, String action, String content, String category, String confidence, String rawResponse) {
        this.employeeName = employeeName;
        this.action = action;
        this.content = content;
        this.category = category;
        this.confidence = confidence;
        this.rawResponse = rawResponse;
    }
    
    // Getter and Setter methods
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    
    // カテゴリの定数
    public static final String CATEGORY_VACATION = "vacation";
    public static final String CATEGORY_HEALTH = "health";
    public static final String CATEGORY_SCHEDULE = "schedule";
    public static final String CATEGORY_PERFORMANCE = "performance";
    public static final String CATEGORY_PERSONAL = "personal";
    public static final String CATEGORY_UNCATEGORIZED = "uncategorized";
    
    // アクションの定数
    public static final String ACTION_ADD_NOTE = "add_note";
    public static final String ACTION_UPDATE_INFO = "update_info";
    public static final String ACTION_SCHEDULE_EVENT = "schedule_event";
    public static final String ACTION_UNCATEGORIZED = "uncategorized";
} 