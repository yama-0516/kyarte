package com.example.kyarte.service;

import com.example.kyarte.dto.AiAnalysisResult;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

@Service
public class MockAiAnalysisService implements AiAnalysisService {
    
    @Override
    public AiAnalysisResult analyzeContent(String content) {
        try {
            System.out.println("=== MockAI Debug ===");
            System.out.println("Input content: " + content);
            
            // 複数の情報を分割して解析
            List<String> separatedContents = separateMultipleEntries(content);
            System.out.println("Separated contents: " + separatedContents.size() + " entries");
            
            // 単一の情報の場合は直接解析
            if (separatedContents.size() == 1) {
                System.out.println("Processing single entry: " + content);
                return analyzeSingleContent(content);
            } else if (separatedContents.size() > 1) {
                // 複数の情報がある場合は、最初の1つを解析
                System.out.println("Processing first entry: " + separatedContents.get(0));
                return analyzeSingleContent(separatedContents.get(0));
            } else {
                // 空の場合は元のテキストを解析
                System.out.println("No separated content, processing original: " + content);
                return analyzeSingleContent(content);
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return new AiAnalysisResult(
                null,
                AiAnalysisResult.ACTION_UNCATEGORIZED,
                content,
                AiAnalysisResult.CATEGORY_UNCATEGORIZED,
                "low",
                "エラー: " + e.getMessage()
            );
        }
    }
    
    /**
     * 複数の情報を全て処理するメソッド（新規追加）
     */
    public List<AiAnalysisResult> analyzeMultipleContent(String content) {
        try {
            System.out.println("=== MockAI Multiple Analysis Debug ===");
            System.out.println("Input content: " + content);
            
            // 複数の情報を分割して解析
            List<String> separatedContents = separateMultipleEntries(content);
            System.out.println("Separated contents: " + separatedContents.size() + " entries");
            
            List<AiAnalysisResult> results = new ArrayList<>();
            
            // 全ての情報を個別に解析
            for (String singleContent : separatedContents) {
                System.out.println("Processing entry: " + singleContent);
                AiAnalysisResult result = analyzeSingleContent(singleContent);
                results.add(result);
            }
            
            return results;
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    private AiAnalysisResult analyzeSingleContent(String content) {
        System.out.println("Analyzing content: " + content);
        
        // 簡単なルールベース解析
        String employeeName = extractEmployeeName(content);
        System.out.println("Extracted employee name: " + employeeName);
        
        String category = determineCategory(content);
        System.out.println("Determined category: " + category);
        
        String action = "add_note";
        String confidence = "medium";
        
        return new AiAnalysisResult(
            employeeName,
            action,
            content,
            category,
            confidence,
            "Mock AI解析結果"
        );
    }
    
    private List<String> separateMultipleEntries(String content) {
        List<String> separated = new ArrayList<>();
        
        System.out.println("=== separateMultipleEntries Debug ===");
        System.out.println("Input content: " + content);
        
        // 句読点、改行、または「。」で分割
        String[] parts = content.split("[。\\n\\r]+");
        System.out.println("Split parts length: " + parts.length);
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            System.out.println("Part " + i + ": '" + part + "'");
            part = part.trim();
            if (!part.isEmpty()) {
                separated.add(part);
                System.out.println("Added to separated: '" + part + "'");
            }
        }
        
        // 分割されない場合は元のテキストをそのまま返す
        if (separated.isEmpty()) {
            separated.add(content);
            System.out.println("No parts found, added original content: '" + content + "'");
        }
        
        System.out.println("Final separated size: " + separated.size());
        for (int i = 0; i < separated.size(); i++) {
            System.out.println("Final part " + i + ": '" + separated.get(i) + "'");
        }
        
        return separated;
    }
    
    private String extractEmployeeName(String content) {
        System.out.println("Extracting name from: " + content);
        
        // パターン1: 「〜さん」のパターンを検索
        Pattern pattern1 = Pattern.compile("([\\u4e00-\\u9fa5]+)さん");
        Matcher matcher1 = pattern1.matcher(content);
        
        if (matcher1.find()) {
            String name = matcher1.group(1);
            System.out.println("Pattern1 matched: " + name);
            return name;
        } else {
            System.out.println("Pattern1: No match");
        }
        
        // パターン2: 「〜氏」のパターンを検索
        Pattern pattern2 = Pattern.compile("([\\u4e00-\\u9fa5]+)氏");
        Matcher matcher2 = pattern2.matcher(content);
        
        if (matcher2.find()) {
            String name = matcher2.group(1);
            System.out.println("Pattern2 matched: " + name);
            return name;
        } else {
            System.out.println("Pattern2: No match");
        }
        
        // パターン3: 一般的な日本の姓のパターンを検索（2-4文字）
        Pattern pattern3 = Pattern.compile("([\\u4e00-\\u9fa5]{2,4})(?:さん|氏|が|は|の|に|を|が|も|と|や|など|ら|たち|達)");
        Matcher matcher3 = pattern3.matcher(content);
        
        if (matcher3.find()) {
            String name = matcher3.group(1);
            System.out.println("Pattern3 matched: " + name);
            return name;
        } else {
            System.out.println("Pattern3: No match");
        }
        
        // パターン4: 単独の漢字名（1-3文字）で、その後に動詞や助詞が続く場合
        Pattern pattern4 = Pattern.compile("([\\u4e00-\\u9fa5]{1,3})(?:が|は|の|に|を|が|も|と|や|など|ら|たち|達|が|は|を|に|で|から|まで|より|へ|と|や|または|及び|及び|並びに)");
        Matcher matcher4 = pattern4.matcher(content);
        
        if (matcher4.find()) {
            String candidate = matcher4.group(1);
            // 一般的な日本の姓かどうかチェック（簡易版）
            if (isCommonJapaneseSurname(candidate)) {
                System.out.println("Pattern4 matched: " + candidate);
                return candidate;
            } else {
                System.out.println("Pattern4: Found " + candidate + " but not in common surnames");
            }
        } else {
            System.out.println("Pattern4: No match");
        }
        
        // パターン5: 文頭の漢字名（1-4文字）
        Pattern pattern5 = Pattern.compile("^([\\u4e00-\\u9fa5\\u30a0-\\u30ff\\u3040-\\u309f]{1,4})");
        Matcher matcher5 = pattern5.matcher(content);
        
        if (matcher5.find()) {
            String candidate = matcher5.group(1);
            System.out.println("Pattern5: Found candidate: " + candidate);
            
            // 4文字から1文字まで順番にチェック
            for (int len = Math.min(candidate.length(), 4); len >= 1; len--) {
                String testName = candidate.substring(0, len);
                System.out.println("Pattern5: Testing length " + len + ": '" + testName + "'");
                
                if (isCommonJapaneseSurname(testName)) {
                    System.out.println("Pattern5 matched: " + testName);
                    return testName;
                } else {
                    System.out.println("Pattern5: '" + testName + "' not in common surnames");
                }
            }
        } else {
            System.out.println("Pattern5: No match");
        }
        
        System.out.println("No name pattern matched");
        return null;
    }
    
    private boolean isCommonJapaneseSurname(String name) {
        // 一般的な日本の姓のリスト（拡張版）
        String[] commonSurnames = {
            // 1文字の姓
            "林", "森", "原", "関", "星", "千", "万", "百", "十", "一",
            
            // 2文字の姓
            "田中", "佐藤", "鈴木", "高橋", "渡辺", "伊藤", "山本", "中村", "小林", "加藤",
            "吉田", "山田", "佐々木", "山口", "松本", "井上", "木村", "斎藤", "清水",
            "山崎", "池田", "橋本", "阿部", "石川", "山下", "中島", "石井", "小川",
            "前田", "岡田", "長谷川", "藤田", "近藤", "坂本", "福田", "太田", "西村", "藤井",
            "岡本", "松田", "中川", "中野", "原田", "小野", "田村", "竹内", "金子", "和田",
            "中山", "石田", "上田", "柴田", "酒井", "工藤", "横山", "宮崎", "宮本",
            "内田", "高木", "安藤", "島田", "谷口", "大野", "高田", "丸山", "今井", "河野",
            "新井", "北村", "武田", "上野", "松井", "荒木", "大塚", "平野", "菅原", "野村",
            "松尾", "菊地", "杉山", "市川", "永井", "小島", "久保", "松岡", "野口", "松原",
            "遠藤", "桜井", "藤原", "青木", "西田", "岩崎", "佐野", "田口", "岡崎", "飯田",
            "平田", "後藤", "早川", "西川", "岡村", "大西", "安田", "内藤", "島崎", "川口",
            "辻本", "星野", "森田", "大橋", "高野", "松村", "岩田", "中尾", "小池",
            "吉川", "西尾", "服部", "古川", "古賀", "平井", "北川", "武藤", "本田",
            "荒井", "大久保", "小西", "新田", "浜田", "森本", "村上", "千葉", "岩本",
            "川崎", "西野", "野崎", "横田", "村松", "大谷", "小田", "松浦",
            "吉岡", "三浦", "小松", "宮田", "大石", "松崎", "田辺", "松野",
            "中井", "小泉", "大森", "川上", "松山", "田原", "西岡", "野田",
            "大島", "田島", "野中", "田代",
            
            // 3文字の姓
            "佐々木", "長谷川", "佐久間", "小野寺", "小野寺", "佐久間", "長谷川",
            
            // 4文字の姓
            "小野寺", "日向寺",

            // カタカナ姓
            "スミス", "ジョンソン", "ウィリアムズ", "ブラウン", "ジョーンズ", "ガルシア", "ミラー", "デイビス",
            "ロドリゲス", "マルティネス", "ヘルナンデス", "ロペス", "ゴンザレス", "ウィルソン", "アンダーソン",
            "トーマス", "テイラー", "ムーア", "ジャクソン", "マーティン", "リー", "ペレス", "トンプソン",
            "ホワイト", "ハリス", "サンチェス", "クラーク", "ラミレス", "ルイス", "ロビンソン", "ウォーカー",
            "ヤング", "アレン", "キング", "ライト", "スコット", "トレス", "グリーン", "ベイカー",
            "アダムズ", "ネルソン", "ヒル", "リバー", "キャンベル", "ミッチェル", "カーター", "ロバーツ",
            "ゴメス", "フィリップス", "エバンス", "ターナー", "ディアス", "パーカー", "クルーズ", "エドワーズ",
            "コリンズ", "レイ", "スチュワート", "サンチェス", "モリス", "ロジャース", "リード", "クック",
            "モーガン", "ベル", "マーフィー", "ベイリー", "リベラ", "クーパー", "リチャードソン", "コックス",
            "ハワード", "ウォード", "トレス", "ピーターソン", "グレイ", "ラミレス", "ジェームズ", "ワトソン",
            "ブルックス", "ケリー", "サンダース", "プライス", "ベネット", "ウッド", "バーンズ", "ロス",
            "ヘンダーソン", "コールマン", "ジェンキンス", "ペリー", "パウエル", "ロング", "パターソン", "ヒューズ",
            "フローレス", "ワシントン", "バトラー", "シモンズ", "フォスター", "ゴンザレス", "ブライアント", "アレクサンダー",
            "ラッセル", "グリフィン", "ディアス", "ヘイズ", "マイヤーズ", "フォード", "ハミルトン", "グラハム",
            "サリバン", "ウォレス", "ウッズ", "コール", "ウェスト", "ジョーダン", "オーウェン", "レイノルズ",
            "フィッシャー", "エリス", "ハリソン", "ギブソン", "マクドナルド", "クルーズ", "マーシャル", "オルソン",
            "ウェブ", "ハート", "ギルバート", "スナイダー", "ピーターソン", "クーパー", "リード", "ベイリー",
            "ケリー", "ハワード", "ラモス", "キム", "コックス", "ワード", "トレス", "ピーターソン",
            "グレイ", "ラミレス", "ジェームズ", "ワトソン", "ブルックス", "ケリー", "サンダース", "プライス",
            "ベネット", "ウッド", "バーンズ", "ロス", "ヘンダーソン", "コールマン", "ジェンキンス", "ペリー",
            "パウエル", "ロング", "パターソン", "ヒューズ", "フローレス", "ワシントン", "バトラー", "シモンズ",
            "フォスター", "ゴンザレス", "ブライアント", "アレクサンダー", "ラッセル", "グリフィン", "ディアス", "ヘイズ",
            "マイヤーズ", "フォード", "ハミルトン", "グラハム", "サリバン", "ウォレス", "ウッズ", "コール",
            "ウェスト", "ジョーダン", "オーウェン", "レイノルズ", "フィッシャー", "エリス", "ハリソン", "ギブソン",
            "マクドナルド", "クルーズ", "マーシャル", "オルソン", "ウェブ", "ハート", "ギルバート", "スナイダー",
            "ピーターソン", "クーパー", "リード", "ベイリー", "ケリー", "ハワード", "ラモス", "キム",
            "コックス", "ワード", "トレス", "ピーターソン", "グレイ", "ラミレス", "ジェームズ", "ワトソン",
            "ブルックス", "ケリー", "サンダース", "プライス", "ベネット", "ウッド", "バーンズ", "ロス",
            "ヘンダーソン", "コールマン", "ジェンキンス", "ペリー", "パウエル", "ロング", "パターソン", "ヒューズ",
            "フローレス", "ワシントン", "バトラー", "シモンズ", "フォスター", "ゴンザレス", "ブライアント", "アレクサンダー",
            "ラッセル", "グリフィン", "ディアス", "ヘイズ", "マイヤーズ", "フォード", "ハミルトン", "グラハム",
            "サリバン", "ウォレス", "ウッズ", "コール", "ウェスト", "ジョーダン", "オーウェン", "レイノルズ",
            "フィッシャー", "エリス", "ハリソン", "ギブソン", "マクドナルド", "クルーズ", "マーシャル", "オルソン",
            "ウェブ", "ハート", "ギルバート", "スナイダー", "ピーターソン", "クーパー", "リード", "ベイリー",
            "ケリー", "ハワード", "ラモス", "キム", "コックス", "ワード", "トレス", "ピーターソン",
            "グレイ", "ラミレス", "ジェームズ", "ワトソン", "ブルックス", "ケリー", "サンダース", "プライス",
            "ベネット", "ウッド", "バーンズ", "ロス", "ヘンダーソン", "コールマン", "ジェンキンス", "ペリー",
            "パウエル", "ロング", "パターソン", "ヒューズ", "フローレス", "ワシントン", "バトラー", "シモンズ",
            "フォスター", "ゴンザレス", "ブライアント", "アレクサンダー", "ラッセル", "グリフィン", "ディアス", "ヘイズ",
            "マイヤーズ", "フォード", "ハミルトン", "グラハム", "サリバン", "ウォレス", "ウッズ", "コール",
            "ウェスト", "ジョーダン", "オーウェン", "レイノルズ", "フィッシャー", "エリス", "ハリソン", "ギブソン",
            "マクドナルド", "クルーズ", "マーシャル", "オルソン", "ウェブ", "ハート", "ギルバート", "スナイダー",
            "ピーターソン", "クーパー", "リード", "ベイリー", "ケリー", "ハワード", "ラモス", "キム",
            "コックス", "ワード", "トレス", "ピーターソン", "グレイ", "ラミレス", "ジェームズ", "ワトソン",
            "ブルックス", "ケリー", "サンダース", "プライス", "ベネット", "ウッド", "バーンズ", "ロス",


        };
        
        for (String surname : commonSurnames) {
            if (surname.equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String determineCategory(String content) {
        String lowerContent = content.toLowerCase();
        
        // 有給・休暇関連
        if (lowerContent.contains("有給") || lowerContent.contains("休暇") || 
            lowerContent.contains("休み") || lowerContent.contains("休む")) {
            return AiAnalysisResult.CATEGORY_VACATION;
        }
        
        // 体調・健康関連
        if (lowerContent.contains("体調") || lowerContent.contains("健康") || 
            lowerContent.contains("病気") || lowerContent.contains("風邪") ||
            lowerContent.contains("具合") || lowerContent.contains("調子")) {
            return AiAnalysisResult.CATEGORY_HEALTH;
        }
        
        // 予定・会議関連
        if (lowerContent.contains("会議") || lowerContent.contains("予定") || 
            lowerContent.contains("打ち合わせ") || lowerContent.contains("ミーティング")) {
            return AiAnalysisResult.CATEGORY_SCHEDULE;
        }
        
        // 評価・成果関連
        if (lowerContent.contains("評価") || lowerContent.contains("成果") || 
            lowerContent.contains("成績") || lowerContent.contains("実績")) {
            return AiAnalysisResult.CATEGORY_PERFORMANCE;
        }
        
        // 個人情報関連
        if (lowerContent.contains("家族") || lowerContent.contains("結婚") || 
            lowerContent.contains("引っ越し") || lowerContent.contains("転勤")) {
            return AiAnalysisResult.CATEGORY_PERSONAL;
        }
        
        return AiAnalysisResult.CATEGORY_UNCATEGORIZED;
    }
    
    @Override
    public String getServiceName() {
        return "Mock AI";
    }
} 