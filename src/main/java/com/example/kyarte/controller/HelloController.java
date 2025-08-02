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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.kyarte.service.CalendarService;
import com.example.kyarte.entity.CalendarEvent;
import java.time.LocalDateTime;

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
    
    @Autowired
    private CalendarService calendarService;
    
    @GetMapping("/")
    public String index(Model model) {
        try {
            // 最新のノートを取得
            model.addAttribute("recentNotes", freeNoteService.getRecentNotes());
        } catch (Exception e) {
            model.addAttribute("recentNotes", new ArrayList<>());
            model.addAttribute("error", "データの取得に失敗しました: " + e.getMessage());
        }
        return "index";
    }
    
    @GetMapping("/database")
    public String database(Model model) {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            model.addAttribute("employees", employees);
            model.addAttribute("totalCount", employees.size());
            
            // 部署別の統計
            Map<String, Long> departmentStats = employees.stream()
                .collect(Collectors.groupingBy(
                    emp -> emp.getDepartment() != null ? emp.getDepartment() : "未設定",
                    Collectors.counting()
                ));
            model.addAttribute("departmentStats", departmentStats);
            
        } catch (Exception e) {
            model.addAttribute("employees", new ArrayList<>());
            model.addAttribute("totalCount", 0);
            model.addAttribute("error", "データの取得に失敗しました: " + e.getMessage());
        }
        return "database";
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
        System.out.println("=== ダミー社員20人作成開始 ===");
        
        try {
            // 既存のデータをクリア
            employeeRepository.deleteAll();
            System.out.println("既存データをクリアしました");
            
            // ダミー社員データを作成
            List<Employee> dummyEmployees = new ArrayList<>();
		
		// 1. 田中 太郎
		Employee emp1 = new Employee();
		emp1.setLastName("田中"); emp1.setFirstName("太郎"); emp1.setBirthDate(LocalDate.of(1985, 3, 15));
		emp1.setDepartment("営業部"); emp1.setPosition("営業マネージャー"); emp1.setEmail("tanaka@company.com");
		emp1.setPhone("090-1234-5678"); emp1.setNotes("営業成績優秀、リーダーシップあり");
		dummyEmployees.add(emp1);
		
		// 2. 佐藤 花子
		Employee emp2 = new Employee();
		emp2.setLastName("佐藤"); emp2.setFirstName("花子"); emp2.setBirthDate(LocalDate.of(1990, 7, 22));
		emp2.setDepartment("人事部"); emp2.setPosition("人事担当"); emp2.setEmail("sato@company.com");
		emp2.setPhone("090-2345-6789"); emp2.setNotes("コミュニケーション能力高、新入社員教育担当");
		dummyEmployees.add(emp2);
		
		// 3. 鈴木 一郎
		Employee emp3 = new Employee();
		emp3.setLastName("鈴木"); emp3.setFirstName("一郎"); emp3.setBirthDate(LocalDate.of(1988, 11, 8));
		emp3.setDepartment("開発部"); emp3.setPosition("シニアエンジニア"); emp3.setEmail("suzuki@company.com");
		emp3.setPhone("090-3456-7890"); emp3.setNotes("Java/Spring Boot専門、技術力高い");
		dummyEmployees.add(emp3);
		
		// 4. 高橋 美咲
		Employee emp4 = new Employee();
		emp4.setLastName("高橋"); emp4.setFirstName("美咲"); emp4.setBirthDate(LocalDate.of(1992, 4, 12));
		emp4.setDepartment("デザイン部"); emp4.setPosition("UI/UXデザイナー"); emp4.setEmail("takahashi@company.com");
		emp4.setPhone("090-4567-8901"); emp4.setNotes("クリエイティブな発想、ユーザビリティ重視");
		dummyEmployees.add(emp4);
		
		// 5. 渡辺 健太
		Employee emp5 = new Employee();
		emp5.setLastName("渡辺"); emp5.setFirstName("健太"); emp5.setBirthDate(LocalDate.of(1987, 9, 30));
		emp5.setDepartment("営業部"); emp5.setPosition("営業担当"); emp5.setEmail("watanabe@company.com");
		emp5.setPhone("090-5678-9012"); emp5.setNotes("粘り強い営業スタイル、顧客との関係構築が得意");
		dummyEmployees.add(emp5);
		
		// 6. 伊藤 恵子
		Employee emp6 = new Employee();
		emp6.setLastName("伊藤"); emp6.setFirstName("恵子"); emp6.setBirthDate(LocalDate.of(1991, 12, 3));
		emp6.setDepartment("経理部"); emp6.setPosition("経理担当"); emp6.setEmail("ito@company.com");
		emp6.setPhone("090-6789-0123"); emp6.setNotes("正確な数字処理、コスト管理意識高い");
		dummyEmployees.add(emp6);
		
		// 7. 山田 大輔
		Employee emp7 = new Employee();
		emp7.setLastName("山田"); emp7.setFirstName("大輔"); emp7.setBirthDate(LocalDate.of(1986, 6, 18));
		emp7.setDepartment("開発部"); emp7.setPosition("フロントエンドエンジニア"); emp7.setEmail("yamada@company.com");
		emp7.setPhone("090-7890-1234"); emp7.setNotes("React/Vue.js専門、最新技術に詳しい");
		dummyEmployees.add(emp7);
		
		// 8. 中村 由美
		Employee emp8 = new Employee();
		emp8.setLastName("中村"); emp8.setFirstName("由美"); emp8.setBirthDate(LocalDate.of(1993, 2, 25));
		emp8.setDepartment("マーケティング部"); emp8.setPosition("マーケティング担当"); emp8.setEmail("nakamura@company.com");
		emp8.setPhone("090-8901-2345"); emp8.setNotes("SNSマーケティング得意、トレンド感覚鋭い");
		dummyEmployees.add(emp8);
		
		// 9. 小林 正男
		Employee emp9 = new Employee();
		emp9.setLastName("小林"); emp9.setFirstName("正男"); emp9.setBirthDate(LocalDate.of(1984, 8, 14));
		emp9.setDepartment("営業部"); emp9.setPosition("営業部長"); emp9.setEmail("kobayashi@company.com");
		emp9.setPhone("090-9012-3456"); emp9.setNotes("営業戦略立案、チームマネジメント経験豊富");
		dummyEmployees.add(emp9);
		
		// 10. 加藤 愛
		Employee emp10 = new Employee();
		emp10.setLastName("加藤"); emp10.setFirstName("愛"); emp10.setBirthDate(LocalDate.of(1994, 5, 7));
		emp10.setDepartment("人事部"); emp10.setPosition("採用担当"); emp10.setEmail("kato@company.com");
		emp10.setPhone("090-0123-4567"); emp10.setNotes("新卒採用担当、面接官として定評あり");
		dummyEmployees.add(emp10);
		
		// 11. 吉田 雄一
		Employee emp11 = new Employee();
		emp11.setLastName("吉田"); emp11.setFirstName("雄一"); emp11.setBirthDate(LocalDate.of(1989, 1, 20));
		emp11.setDepartment("開発部"); emp11.setPosition("バックエンドエンジニア"); emp11.setEmail("yoshida@company.com");
		emp11.setPhone("090-1234-5678"); emp11.setNotes("Python/Django専門、データベース設計得意");
		dummyEmployees.add(emp11);
		
		// 12. 山本 真理
		Employee emp12 = new Employee();
		emp12.setLastName("山本"); emp12.setFirstName("真理"); emp12.setBirthDate(LocalDate.of(1990, 10, 11));
		emp12.setDepartment("デザイン部"); emp12.setPosition("グラフィックデザイナー"); emp12.setEmail("yamamoto@company.com");
		emp12.setPhone("090-2345-6789"); emp12.setNotes("ブランディングデザイン、ロゴ制作実績多数");
		dummyEmployees.add(emp12);
		
		// 13. 松本 和也
		Employee emp13 = new Employee();
		emp13.setLastName("松本"); emp13.setFirstName("和也"); emp13.setBirthDate(LocalDate.of(1987, 4, 5));
		emp13.setDepartment("営業部"); emp13.setPosition("営業担当"); emp13.setEmail("matsumoto@company.com");
		emp13.setPhone("090-3456-7890"); emp13.setNotes("法人営業専門、大企業との取引実績あり");
		dummyEmployees.add(emp13);
		
		// 14. 井上 麻衣
		Employee emp14 = new Employee();
		emp14.setLastName("井上"); emp14.setFirstName("麻衣"); emp14.setBirthDate(LocalDate.of(1992, 8, 28));
		emp14.setDepartment("経理部"); emp14.setPosition("経理担当"); emp14.setEmail("inoue@company.com");
		emp14.setPhone("090-4567-8901"); emp14.setNotes("税務申告、決算業務経験豊富");
		dummyEmployees.add(emp14);
		
		// 15. 木村 達也
		Employee emp15 = new Employee();
		emp15.setLastName("木村"); emp15.setFirstName("達也"); emp15.setBirthDate(LocalDate.of(1985, 12, 16));
		emp15.setDepartment("開発部"); emp15.setPosition("インフラエンジニア"); emp15.setEmail("kimura@company.com");
		emp15.setPhone("090-5678-9012"); emp15.setNotes("AWS/Azure専門、クラウドインフラ構築得意");
		dummyEmployees.add(emp15);
		
		// 16. 林 美穂
		Employee emp16 = new Employee();
		emp16.setLastName("林"); emp16.setFirstName("美穂"); emp16.setBirthDate(LocalDate.of(1991, 3, 9));
		emp16.setDepartment("マーケティング部"); emp16.setPosition("コンテンツマーケティング"); emp16.setEmail("hayashi@company.com");
		emp16.setPhone("090-6789-0123"); emp16.setNotes("コンテンツ制作、SEO対策専門");
		dummyEmployees.add(emp16);
		
		// 17. 斎藤 誠
		Employee emp17 = new Employee();
		emp17.setLastName("斎藤"); emp17.setFirstName("誠"); emp17.setBirthDate(LocalDate.of(1988, 7, 2));
		emp17.setDepartment("営業部"); emp17.setPosition("営業担当"); emp17.setEmail("saito@company.com");
		emp17.setPhone("090-7890-1234"); emp17.setNotes("新規開拓営業、スタートアップ企業との取引実績");
		dummyEmployees.add(emp17);
		
		// 18. 清水 香織
		Employee emp18 = new Employee();
		emp18.setLastName("清水"); emp18.setFirstName("香織"); emp18.setBirthDate(LocalDate.of(1993, 11, 19));
		emp18.setDepartment("人事部"); emp18.setPosition("人事担当"); emp18.setEmail("shimizu@company.com");
		emp18.setPhone("090-8901-2345"); emp18.setNotes("研修企画、人材育成プログラム開発");
		dummyEmployees.add(emp18);
		
		// 19. 森 健二
		Employee emp19 = new Employee();
		emp19.setLastName("森"); emp19.setFirstName("健二"); emp19.setBirthDate(LocalDate.of(1986, 2, 13));
		emp19.setDepartment("開発部"); emp19.setPosition("QAエンジニア"); emp19.setEmail("mori@company.com");
		emp19.setPhone("090-9012-3456"); emp19.setNotes("テスト自動化、品質保証プロセス改善");
		dummyEmployees.add(emp19);
		
		// 20. 池田 美咲
		Employee emp20 = new Employee();
		emp20.setLastName("池田"); emp20.setFirstName("美咲"); emp20.setBirthDate(LocalDate.of(1990, 9, 6));
		emp20.setDepartment("デザイン部"); emp20.setPosition("Webデザイナー"); emp20.setEmail("ikeda@company.com");
		emp20.setPhone("090-0123-4567"); emp20.setNotes("レスポンシブデザイン、アクセシビリティ対応");
		dummyEmployees.add(emp20);
        
            // データベースに保存
            employeeRepository.saveAll(dummyEmployees);
            
            System.out.println("=== ダミー社員20人作成完了 ===");
            System.out.println("作成された社員数: " + dummyEmployees.size());
            
            return "ダミー社員20人を作成しました！作成数: " + dummyEmployees.size();
            
        } catch (Exception e) {
            System.err.println("ダミー社員作成エラー: " + e.getMessage());
            e.printStackTrace();
            return "エラー: " + e.getMessage();
        }
    }
    
    @GetMapping("/debug/employee-count")
    @ResponseBody
    public String getEmployeeCount() {
        try {
            long count = employeeRepository.count();
            List<Employee> allEmployees = employeeRepository.findAll();
            
            StringBuilder result = new StringBuilder();
            result.append("=== 社員数確認 ===\n");
            result.append("総社員数: ").append(count).append("\n\n");
            result.append("=== 全社員一覧 ===\n");
            
            for (Employee emp : allEmployees) {
                result.append("ID: ").append(emp.getId())
                      .append(", 名前: ").append(emp.getFullName())
                      .append(", 部署: ").append(emp.getDepartment())
                      .append(", 役職: ").append(emp.getPosition())
                      .append("\n");
            }
            
            return result.toString();
            
        } catch (Exception e) {
            return "エラー: " + e.getMessage();
        }
    }
    
    @GetMapping("/error")
    public String error(Model model) {
        model.addAttribute("errorMessage", "ページが見つかりませんでした。");
        model.addAttribute("errorCode", "404");
        return "error";
    }
    
    @GetMapping("/debug/init-dummy-events")
    @ResponseBody
    public String initDummyEvents() {
        System.out.println("=== ダミーカレンダーイベント作成開始 ===");
        
        try {
            // 既存のイベントをクリア
            calendarService.getAllEvents().forEach(event -> calendarService.deleteEvent(event.getId()));
            System.out.println("既存イベントをクリアしました");
            
            // 従業員データを取得
            List<Employee> employees = employeeService.getAllEmployees();
            if (employees.isEmpty()) {
                return "エラー: 従業員データがありません。先にダミー社員を作成してください。";
            }
            
            int createdCount = 0;
            
            // 1. 田中太郎の有給申請
            CalendarEvent event1 = new CalendarEvent();
            event1.setTitle("田中太郎 有給申請");
            event1.setDescription("家族旅行のため有給を取得します。よろしくお願いします。");
            event1.setStartTime(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0));
            event1.setEndTime(LocalDateTime.now().plusDays(5).withHour(18).withMinute(0));
            event1.setEventType("vacation");
            event1.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("太郎")).findFirst().orElse(employees.get(0)));
            calendarService.saveEvent(event1);
            createdCount++;
            
            // 2. 佐藤花子の長女誕生日
            CalendarEvent event2 = new CalendarEvent();
            event2.setTitle("佐藤花子 長女誕生日");
            event2.setDescription("長女の5歳の誕生日。早退してケーキ作りとプレゼント準備。");
            event2.setStartTime(LocalDateTime.now().plusDays(3).withHour(15).withMinute(0));
            event2.setEndTime(LocalDateTime.now().plusDays(3).withHour(17).withMinute(0));
            event2.setEventType("other");
            event2.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("花子")).findFirst().orElse(employees.get(1)));
            calendarService.saveEvent(event2);
            createdCount++;
            
            // 3. 鈴木一郎の技術勉強会
            CalendarEvent event3 = new CalendarEvent();
            event3.setTitle("鈴木一郎 技術勉強会");
            event3.setDescription("Spring Boot最新機能について勉強会を開催。参加者募集中。");
            event3.setStartTime(LocalDateTime.now().plusDays(7).withHour(14).withMinute(0));
            event3.setEndTime(LocalDateTime.now().plusDays(7).withHour(16).withMinute(0));
            event3.setLocation("会議室A");
            event3.setEventType("meeting");
            event3.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("一郎")).findFirst().orElse(employees.get(2)));
            calendarService.saveEvent(event3);
            createdCount++;
            
            // 4. 高橋美咲のデザイン締切
            CalendarEvent event4 = new CalendarEvent();
            event4.setTitle("高橋美咲 新商品デザイン締切");
            event4.setDescription("新商品のパッケージデザイン提出期限。クライアントとの最終確認。");
            event4.setStartTime(LocalDateTime.now().plusDays(2).withHour(17).withMinute(0));
            event4.setEndTime(LocalDateTime.now().plusDays(2).withHour(18).withMinute(0));
            event4.setEventType("deadline");
            event4.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("美咲")).findFirst().orElse(employees.get(3)));
            calendarService.saveEvent(event4);
            createdCount++;
            
            // 5. 渡辺健太の部下面談
            CalendarEvent event5 = new CalendarEvent();
            event5.setTitle("渡辺健太 部下面談");
            event5.setDescription("新入社員の山田さんの定例面談。成長状況と今後の目標について。");
            event5.setStartTime(LocalDateTime.now().plusDays(4).withHour(10).withMinute(0));
            event5.setEndTime(LocalDateTime.now().plusDays(4).withHour(11).withMinute(0));
            event5.setLocation("面談室");
            event5.setEventType("meeting");
            event5.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("健太")).findFirst().orElse(employees.get(4)));
            calendarService.saveEvent(event5);
            createdCount++;
            
            // 6. 伊藤恵子の歯医者
            CalendarEvent event6 = new CalendarEvent();
            event6.setTitle("伊藤恵子 歯医者予約");
            event6.setDescription("定期検診のため歯医者に行きます。午後から出社予定。");
            event6.setStartTime(LocalDateTime.now().plusDays(6).withHour(9).withMinute(30));
            event6.setEndTime(LocalDateTime.now().plusDays(6).withHour(11).withMinute(0));
            event6.setEventType("other");
            event6.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("恵子")).findFirst().orElse(employees.get(5)));
            calendarService.saveEvent(event6);
            createdCount++;
            
            // 7. 山田大輔の子供の運動会
            CalendarEvent event7 = new CalendarEvent();
            event7.setTitle("山田大輔 子供の運動会");
            event7.setDescription("長男の小学校運動会。応援に行くため半休を取得。");
            event7.setStartTime(LocalDateTime.now().plusDays(8).withHour(13).withMinute(0));
            event7.setEndTime(LocalDateTime.now().plusDays(8).withHour(17).withMinute(0));
            event7.setEventType("other");
            event7.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("大輔")).findFirst().orElse(employees.get(6)));
            calendarService.saveEvent(event7);
            createdCount++;
            
            // 8. 中村由美のSNS投稿締切
            CalendarEvent event8 = new CalendarEvent();
            event8.setTitle("中村由美 新商品SNS投稿締切");
            event8.setDescription("新商品のInstagram投稿用写真撮影と投稿文案作成。");
            event8.setStartTime(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0));
            event8.setEndTime(LocalDateTime.now().plusDays(1).withHour(17).withMinute(30));
            event8.setEventType("deadline");
            event8.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("由美")).findFirst().orElse(employees.get(7)));
            calendarService.saveEvent(event8);
            createdCount++;
            
            // 9. 小林正男の営業会議
            CalendarEvent event9 = new CalendarEvent();
            event9.setTitle("小林正男 月次営業会議");
            event9.setDescription("今月の営業実績報告と来月の戦略会議。全営業担当者参加。");
            event9.setStartTime(LocalDateTime.now().plusDays(10).withHour(14).withMinute(0));
            event9.setEndTime(LocalDateTime.now().plusDays(10).withHour(16).withMinute(0));
            event9.setLocation("大会議室");
            event9.setEventType("meeting");
            event9.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("正男")).findFirst().orElse(employees.get(8)));
            calendarService.saveEvent(event9);
            createdCount++;
            
            // 10. 加藤愛の採用面接
            CalendarEvent event10 = new CalendarEvent();
            event10.setTitle("加藤愛 新卒採用面接");
            event10.setDescription("新卒採用の最終面接。技術職候補者3名の面接を実施。");
            event10.setStartTime(LocalDateTime.now().plusDays(9).withHour(10).withMinute(0));
            event10.setEndTime(LocalDateTime.now().plusDays(9).withHour(12).withMinute(0));
            event10.setLocation("面接室");
            event10.setEventType("meeting");
            event10.setEmployee(employees.stream().filter(e -> e.getFirstName().equals("愛")).findFirst().orElse(employees.get(9)));
            calendarService.saveEvent(event10);
            createdCount++;
            
            System.out.println("=== ダミーカレンダーイベント作成完了 ===");
            System.out.println("作成されたイベント数: " + createdCount);
            
            return "ダミーカレンダーイベント" + createdCount + "件を作成しました！";
            
        } catch (Exception e) {
            System.err.println("ダミーイベント作成エラー: " + e.getMessage());
            e.printStackTrace();
            return "エラー: " + e.getMessage();
        }
    }
}
