package com.example.kyarte.controller;

import com.example.kyarte.entity.FreeNote;
import com.example.kyarte.service.FreeNoteService;
import com.example.kyarte.service.AiDataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.kyarte.entity.Employee;
import com.example.kyarte.service.EmployeeService;
import com.example.kyarte.service.AiAnalysisService;
import com.example.kyarte.dto.AiAnalysisResult;
import com.example.kyarte.service.MockAiAnalysisService;
import com.example.kyarte.service.GeminiAnalysisService;
import com.example.kyarte.repository.EmployeeRepository;

@Controller
public class HelloController {
    
    @Autowired
    private FreeNoteService freeNoteService;
    
    @Autowired
    private AiDataProcessingService aiDataProcessingService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AiAnalysisService aiAnalysisService;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @GetMapping("/")
    public String index(Model model) {
        // 最新のノートを取得
        model.addAttribute("recentNotes", freeNoteService.getRecentNotes());
        return "index";
    }
    
    @PostMapping("/save-note")
    @ResponseBody
    public String saveNote(@RequestParam String content, @RequestParam String inputType) {
        try {
            System.out.println("=== HelloController Debug ===");
            System.out.println("Received content (raw): " + content);
            System.out.println("Received content (bytes): " + java.util.Arrays.toString(content.getBytes("UTF-8")));
            System.out.println("Received inputType: " + inputType);
            
            // FreeNoteを保存
            FreeNote note = new FreeNote();
            note.setContent(content);
            note.setInputType(inputType);
            FreeNote savedNote = freeNoteService.saveFreeNote(note);
            System.out.println("Saved FreeNote ID: " + savedNote.getId());
            
            // AI解析を実行（同期で実行してデバッグ）
            System.out.println("Starting AI processing...");
            try {
                System.out.println("AI processing started");
                aiDataProcessingService.processFreeNoteWithAi(savedNote);
                System.out.println("AI processing completed successfully");
            } catch (Exception e) {
                System.err.println("AI解析エラー: " + e.getMessage());
                e.printStackTrace();
            }
            
            return "success";
        } catch (Exception e) {
            System.err.println("Controller error: " + e.getMessage());
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
    
    @GetMapping("/hello")
    public String hello() {
        return "Kyarte起動成功！👋";
    }
    
    // 簡単なテストエンドポイント
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "HelloController is working!";
    }
    
    // デバッグ用：従業員検索テスト
    @GetMapping("/debug/employees")
    @ResponseBody
    public String debugEmployees() {
        try {
            List<Employee> allEmployees = employeeService.getAllEmployees();
            StringBuilder result = new StringBuilder();
            result.append("=== 全従業員 ===\n");
            
            for (Employee emp : allEmployees) {
                result.append("ID: ").append(emp.getId())
                      .append(", 名前: ").append(emp.getFullName())
                      .append(", 姓: ").append(emp.getLastName())
                      .append(", 名: ").append(emp.getFirstName())
                      .append(", メモ: ").append(emp.getNotes())
                      .append("\n");
            }
            
            // 佐藤で検索テスト
            List<Employee> satoEmployees = employeeService.searchEmployeesByName("佐藤");
            result.append("\n=== 佐藤で検索結果 ===\n");
            result.append("検索結果数: ").append(satoEmployees.size()).append("\n");
            
            for (Employee emp : satoEmployees) {
                result.append("ID: ").append(emp.getId())
                      .append(", 名前: ").append(emp.getFullName())
                      .append("\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }
    
    // デバッグ用：AI解析テスト
    @GetMapping("/debug/ai-test")
    @ResponseBody
    public String debugAiTest() {
        try {
            String testContent = "佐藤来週有給";
            System.out.println("=== AI解析テスト デバッグ ===");
            System.out.println("Test content (raw): " + testContent);
            System.out.println("Test content (bytes): " + java.util.Arrays.toString(testContent.getBytes("UTF-8")));
            
            AiAnalysisResult result = aiAnalysisService.analyzeContent(testContent);
            
            StringBuilder debug = new StringBuilder();
            debug.append("=== AI解析テスト ===\n");
            debug.append("入力: ").append(testContent).append("\n");
            debug.append("抽出された名前: ").append(result.getEmployeeName()).append("\n");
            debug.append("カテゴリ: ").append(result.getCategory()).append("\n");
            debug.append("内容: ").append(result.getContent()).append("\n");
            
            return debug.toString();
            
        } catch (Exception e) {
            return "AI解析エラー: " + e.getMessage();
        }
    }
    
    // デバッグ用：名前抽出テスト
    @GetMapping("/debug/name-extraction")
    @ResponseBody
    public String debugNameExtraction() {
        try {
            String testContent = "佐藤来週有給";
            System.out.println("=== 名前抽出テスト ===");
            System.out.println("Test content: " + testContent);
            
            // 正規表現パターンを修正（2文字の姓を優先）
            java.util.regex.Pattern pattern5 = java.util.regex.Pattern.compile("^([\\u4e00-\\u9fa5]{2})");
            java.util.regex.Matcher matcher5 = pattern5.matcher(testContent);
            
            StringBuilder result = new StringBuilder();
            result.append("=== 名前抽出テスト ===\n");
            result.append("入力: ").append(testContent).append("\n");
            
            if (matcher5.find()) {
                String candidate = matcher5.group(1);
                result.append("Pattern5: Found candidate: ").append(candidate).append("\n");
                
                // 一般的な日本の姓かどうかチェック
                String[] commonSurnames = {"田中", "佐藤", "鈴木", "高橋", "渡辺", "伊藤", "山本", "中村", "小林", "加藤"};
                boolean isCommon = false;
                for (String surname : commonSurnames) {
                    if (surname.equals(candidate)) {
                        isCommon = true;
                        break;
                    }
                }
                
                if (isCommon) {
                    result.append("Pattern5 matched: ").append(candidate).append("\n");
                    result.append("結果: ").append(candidate).append("\n");
                } else {
                    result.append("Pattern5: ").append(candidate).append(" not in common surnames\n");
                    result.append("結果: null\n");
                }
            } else {
                result.append("Pattern5: No match\n");
                result.append("結果: null\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "名前抽出エラー: " + e.getMessage();
        }
    }
    
    // デバッグ用：MockAI直接テスト
    @GetMapping("/debug/mockai-direct")
    @ResponseBody
    public String debugMockAiDirect() {
        try {
            String testContent = "佐藤来週有給";
            System.out.println("=== MockAI直接テスト ===");
            System.out.println("Test content: " + testContent);
            
            // MockAiAnalysisServiceを直接インスタンス化してテスト
            MockAiAnalysisService mockService = new MockAiAnalysisService();
            AiAnalysisResult result = mockService.analyzeContent(testContent);
            
            StringBuilder debug = new StringBuilder();
            debug.append("=== MockAI直接テスト ===\n");
            debug.append("入力: ").append(testContent).append("\n");
            debug.append("抽出された名前: ").append(result.getEmployeeName()).append("\n");
            debug.append("カテゴリ: ").append(result.getCategory()).append("\n");
            debug.append("内容: ").append(result.getContent()).append("\n");
            
            return debug.toString();
            
        } catch (Exception e) {
            return "MockAI直接テストエラー: " + e.getMessage();
        }
    }
    
    // デバッグ用：名前抽出詳細テスト
    @GetMapping("/debug/name-extraction-detailed")
    @ResponseBody
    public String debugNameExtractionDetailed() {
        try {
            String testContent = "佐藤来週有給";
            System.out.println("=== 名前抽出詳細テスト ===");
            System.out.println("Test content: " + testContent);
            
            StringBuilder result = new StringBuilder();
            result.append("=== 名前抽出詳細テスト ===\n");
            result.append("入力: ").append(testContent).append("\n\n");
            
            // Pattern5を直接テスト
            java.util.regex.Pattern pattern5 = java.util.regex.Pattern.compile("^([\\u4e00-\\u9fa5]{1,4})");
            java.util.regex.Matcher matcher5 = pattern5.matcher(testContent);
            
            result.append("Pattern5正規表現: ^([\\\\u4e00-\\\\u9fa5]{1,4})\n");
            
            if (matcher5.find()) {
                String candidate = matcher5.group(1);
                result.append("Pattern5: Found candidate: '").append(candidate).append("'\n");
                
                // 一般的な日本の姓かどうかチェック
                String[] commonSurnames = {"田中", "佐藤", "鈴木", "高橋", "渡辺", "伊藤", "山本", "中村", "小林", "加藤"};
                boolean isCommon = false;
                for (String surname : commonSurnames) {
                    if (surname.equals(candidate)) {
                        isCommon = true;
                        result.append("Pattern5: Found in common surnames: '").append(surname).append("'\n");
                        break;
                    }
                }
                
                if (isCommon) {
                    result.append("Pattern5 matched: ").append(candidate).append("\n");
                    result.append("結果: ").append(candidate).append("\n");
                } else {
                    result.append("Pattern5: '").append(candidate).append("' not in common surnames\n");
                    result.append("結果: null\n");
                }
            } else {
                result.append("Pattern5: No match\n");
                result.append("結果: null\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "名前抽出詳細テストエラー: " + e.getMessage();
        }
    }
    
    // デバッグ用：Gemini AIレスポンス確認
    @GetMapping("/debug/gemini-response")
    @ResponseBody
    public String debugGeminiResponse() {
        try {
            String testContent = "佐藤来週有給";
            System.out.println("=== Gemini AIレスポンス確認 ===");
            System.out.println("Test content: " + testContent);
            
            // Springの依存性注入を使用
            AiAnalysisResult result = aiAnalysisService.analyzeContent(testContent);
            
            StringBuilder debug = new StringBuilder();
            debug.append("=== Gemini AIレスポンス確認 ===\n");
            debug.append("入力: ").append(testContent).append("\n");
            debug.append("使用サービス: ").append(aiAnalysisService.getServiceName()).append("\n");
            debug.append("抽出された名前: ").append(result.getEmployeeName()).append("\n");
            debug.append("カテゴリ: ").append(result.getCategory()).append("\n");
            debug.append("内容: ").append(result.getContent()).append("\n");
            debug.append("生レスポンス: ").append(result.getRawResponse()).append("\n");
            
            return debug.toString();
            
        } catch (Exception e) {
            return "Gemini AIレスポンス確認エラー: " + e.getMessage();
        }
    }
    
    // デバッグ用：APIキー確認
    @GetMapping("/debug/api-key-check")
    @ResponseBody
    public String debugApiKeyCheck() {
        try {
            StringBuilder debug = new StringBuilder();
            debug.append("=== APIキー確認 ===\n");
            
            // 環境変数から直接取得
            String envApiKey = System.getenv("GEMINI_API_KEY");
            debug.append("環境変数 GEMINI_API_KEY: ");
            if (envApiKey != null && !envApiKey.isEmpty()) {
                debug.append("設定済み（長さ: ").append(envApiKey.length()).append("文字）\n");
            } else {
                debug.append("未設定\n");
            }
            
            // Springの@Valueで取得（依存性注入を使用）
            try {
                if (aiAnalysisService instanceof GeminiAnalysisService) {
                    GeminiAnalysisService geminiService = (GeminiAnalysisService) aiAnalysisService;
                    // リフレクションでapiKeyフィールドにアクセス
                    java.lang.reflect.Field apiKeyField = GeminiAnalysisService.class.getDeclaredField("apiKey");
                    apiKeyField.setAccessible(true);
                    String springApiKey = (String) apiKeyField.get(geminiService);
                    debug.append("Spring @Value apiKey: ");
                    if (springApiKey != null && !springApiKey.isEmpty()) {
                        debug.append("設定済み（長さ: ").append(springApiKey.length()).append("文字）\n");
                    } else {
                        debug.append("未設定\n");
                    }
                } else {
                    debug.append("Spring @Value apiKey: 現在のサービスは ").append(aiAnalysisService.getServiceName()).append("\n");
                }
            } catch (Exception e) {
                debug.append("Spring @Value apiKey: エラー - ").append(e.getMessage()).append("\n");
            }
            
            return debug.toString();
            
        } catch (Exception e) {
            return "APIキー確認エラー: " + e.getMessage();
        }
    }

        // デバッグ用：AiDataProcessingService処理確認
        @GetMapping("/debug/ai-processing")
        @ResponseBody
        public String debugAiProcessing() {
            try {
                StringBuilder debug = new StringBuilder();
                debug.append("=== AiDataProcessingService処理確認 ===\n");
                
                // 未処理のFreeNoteを取得
                List<FreeNote> unprocessedNotes = freeNoteService.getUnprocessedNotes();
                debug.append("未処理のFreeNote数: ").append(unprocessedNotes.size()).append("\n");
                
                for (FreeNote note : unprocessedNotes) {
                    debug.append("ID: ").append(note.getId())
                          .append(", 内容: ").append(note.getContent())
                          .append(", 処理済み: ").append(note.getProcessed())
                          .append("\n");
                }
                
                // 処理済みのFreeNoteも確認
                List<FreeNote> allNotes = freeNoteService.getAllFreeNotes();
                debug.append("\n全FreeNote数: ").append(allNotes.size()).append("\n");
                
                for (FreeNote note : allNotes) {
                    debug.append("ID: ").append(note.getId())
                          .append(", 内容: ").append(note.getContent())
                          .append(", 処理済み: ").append(note.getProcessed())
                          .append(", 処理時刻: ").append(note.getProcessedAt())
                          .append("\n");
                }
                
                // 従業員データも確認
                List<Employee> allEmployees = employeeService.getAllEmployees();
                debug.append("\n全従業員数: ").append(allEmployees.size()).append("\n");
                
                for (Employee emp : allEmployees) {
                    debug.append("ID: ").append(emp.getId())
                          .append(", 名前: ").append(emp.getFullName())
                          .append(", メモ: ").append(emp.getNotes())
                          .append("\n");
                }
                
                return debug.toString();
                
            } catch (Exception e) {
                return "AiDataProcessingService確認エラー: " + e.getMessage();
            }
        }

    // ダミー従業員を再登録
    @GetMapping("/debug/init-dummy-employees")
    @ResponseBody
    public String initDummyEmployees() {
        try {
            // 既存の従業員を削除
            employeeRepository.deleteAll();
            
            // ダミー従業員を作成
            Employee employee1 = new Employee();
            employee1.setLastName("佐藤");
            employee1.setFirstName("一");
            employee1.setDepartment("営業部");
            employee1.setPosition("主任");
            employee1.setEmail("sato.ichi@example.com");
            employee1.setPhone("090-1234-5678");
            employee1.setNotes("営業成績優秀。顧客からの信頼も厚い。");
            employeeRepository.save(employee1);
            
            Employee employee2 = new Employee();
            employee2.setLastName("田中");
            employee2.setFirstName("次郎");
            employee2.setDepartment("開発部");
            employee2.setPosition("エンジニア");
            employee2.setEmail("tanaka.jiro@example.com");
            employee2.setPhone("090-2345-6789");
            employee2.setNotes("Java開発が得意。新しい技術の習得も積極的。");
            employeeRepository.save(employee2);
            
            Employee employee3 = new Employee();
            employee3.setLastName("鈴木");
            employee3.setFirstName("三郎");
            employee3.setDepartment("人事部");
            employee3.setPosition("課長");
            employee3.setEmail("suzuki.saburo@example.com");
            employee3.setPhone("090-3456-7890");
            employee3.setNotes("人事制度の改善に取り組んでいる。");
            employeeRepository.save(employee3);
            
            Employee employee4 = new Employee();
            employee4.setLastName("スミス");
            employee4.setFirstName("ポール");
            employee4.setDepartment("営業部");
            employee4.setPosition("マネージャー");
            employee4.setEmail("smith.paul@example.com");
            employee4.setPhone("090-4567-8901");
            employee4.setNotes("海外営業を担当。英語が堪能。");
            employeeRepository.save(employee4);
            
            return "ダミー従業員を再登録しました！";
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }
}
