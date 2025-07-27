package com.example.kyarte.service;

import com.example.kyarte.dto.AiAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@Primary
public class GeminiAnalysisService implements AiAnalysisService {

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiAnalysisService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent")
            .defaultHeader("Content-Type", "application/json; charset=UTF-8")
            .defaultHeader("Accept", "application/json; charset=UTF-8")
            .defaultHeader("Accept-Charset", "UTF-8")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AiAnalysisResult analyzeContent(String content) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                System.out.println("Gemini API key not configured, falling back to mock analysis");
                return createMockResult(content);
            }

            String prompt = createAnalysisPrompt(content);
            String responseText = callGeminiApi(prompt);
            return parseGeminiResponse(responseText, content);

        } catch (Exception e) {
            System.err.println("Gemini AI error: " + e.getMessage());
            e.printStackTrace();
            return createMockResult(content);
        }
    }

    @Override
    public List<AiAnalysisResult> analyzeMultipleContent(String content) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                System.out.println("Gemini API key not configured, falling back to mock analysis");
                return createMockMultipleResults(content);
            }

            String prompt = createMultipleAnalysisPrompt(content);
            String responseText = callGeminiApi(prompt);
            return parseGeminiMultipleResponse(responseText, content);

        } catch (Exception e) {
            System.err.println("Gemini AI error: " + e.getMessage());
            e.printStackTrace();
            return createMockMultipleResults(content);
        }
    }

    @Override
    public String getServiceName() {
        return "Gemini AI";
    }

    private String callGeminiApi(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);
            contents.put("parts", parts);

            List<Map<String, Object>> contentsList = new ArrayList<>();
            contentsList.add(contents);
            requestBody.put("contents", contentsList);

            System.out.println("=== callGeminiApi Debug ===");
            System.out.println("Request body: " + objectMapper.writeValueAsString(requestBody));

            return webClient.post()
                .uri("?key=" + apiKey)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json; charset=UTF-8")
                .header("Accept-Charset", "UTF-8")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            System.err.println("callGeminiApi error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String createAnalysisPrompt(String content) {
        return """
            以下のテキストから従業員の情報を抽出してください。

            入力テキスト: %s

            以下のJSON形式で回答してください：
            {
                "employeeName": "従業員名（姓のみ）",
                "action": "add_note",
                "content": "元のテキスト",
                "category": "vacation|health|schedule|performance|personal|uncategorized",
                "confidence": "high|medium|low"
            }

            カテゴリの判定基準：
            - vacation: 有給、休暇、休み関連
            - health: 体調、健康、病気関連
            - schedule: 会議、予定、打ち合わせ関連
            - performance: 評価、成果、成績関連
            - personal: 個人的な情報
            - uncategorized: 上記に該当しない場合
            """.formatted(content);
    }

    private String createMultipleAnalysisPrompt(String content) {
        return """
            以下のテキストには複数の従業員の情報が含まれています。
            各情報を個別に抽出してください。

            入力テキスト: %s

            以下のJSON形式で回答してください：
            [
                {
                    "employeeName": "従業員名（姓のみ）",
                    "action": "add_note",
                    "content": "該当する部分のテキスト",
                    "category": "vacation|health|schedule|performance|personal|uncategorized",
                    "confidence": "high|medium|low"
                }
            ]

            カテゴリの判定基準：
            - vacation: 有給、休暇、休み関連
            - health: 体調、健康、病気関連
            - schedule: 会議、予定、打ち合わせ関連
            - performance: 評価、成果、成績関連
            - personal: 個人的な情報
            - uncategorized: 上記に該当しない場合
            """.formatted(content);
    }

    private AiAnalysisResult parseGeminiResponse(String responseText, String originalContent) {
        try {
            System.out.println("=== parseGeminiResponse Debug ===");
            System.out.println("Raw response: " + responseText);

            // まず、Gemini AIのレスポンス全体をJSONとして解析
            JsonNode responseNode = objectMapper.readTree(responseText);
            
            // candidates[0].content.parts[0].text からJSONを抽出
            String jsonText = responseNode
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();
            
            System.out.println("Extracted text from response: " + jsonText);
            
            // ```json\n{...}\n``` の形式からJSONを抽出
            String jsonContent = extractJsonFromMarkdown(jsonText);
            System.out.println("Extracted JSON content: " + jsonContent);
            
            // JSONを解析
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // 必要なフィールドを抽出
            String employeeName = jsonNode.path("employeeName").asText();
            String category = jsonNode.path("category").asText();
            String confidence = jsonNode.path("confidence").asText();
            
            System.out.println("Parsed values - employeeName: " + employeeName + ", category: " + category + ", confidence: " + confidence);
            
            return new AiAnalysisResult(
                employeeName,
                "add_note",
                originalContent,
                category != null && !category.isEmpty() ? category : "uncategorized",
                confidence != null && !confidence.isEmpty() ? confidence : "medium",
                responseText
            );
            
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response: " + e.getMessage());
            e.printStackTrace();
            return createMockResult(originalContent);
        }
    }
    
    private String extractJsonFromMarkdown(String markdownText) {
        try {
            // ```json\n{...}\n``` の形式からJSONを抽出
            Pattern pattern = Pattern.compile("```json\\s*\\n(.*?)\\n```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(markdownText);
            
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            
            // マークダウンがない場合は、そのまま返す
            return markdownText.trim();
            
        } catch (Exception e) {
            System.err.println("Failed to extract JSON from markdown: " + e.getMessage());
            return markdownText;
        }
    }

    private List<AiAnalysisResult> parseGeminiMultipleResponse(String responseText, String originalContent) {
        try {
            List<AiAnalysisResult> results = new ArrayList<>();

            // まず、Gemini AIのレスポンス全体をJSONとして解析
            JsonNode responseNode = objectMapper.readTree(responseText);
            
            // candidates[0].content.parts[0].text からJSONを抽出
            String jsonText = responseNode
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();
            
            System.out.println("=== parseGeminiMultipleResponse Debug ===");
            System.out.println("Extracted text from response: " + jsonText);
            
            // ```json\n[...]\n``` の形式からJSONを抽出
            String jsonContent = extractJsonFromMarkdown(jsonText);
            System.out.println("Extracted JSON content: " + jsonContent);
            
            // JSON配列を解析
            JsonNode jsonArray = objectMapper.readTree(jsonContent);
            
            if (jsonArray.isArray()) {
                for (JsonNode item : jsonArray) {
                    String employeeName = item.path("employeeName").asText();
                    String category = item.path("category").asText();
                    String confidence = item.path("confidence").asText();
                    String content = item.path("content").asText();
                    
                    System.out.println("Parsed item - employeeName: " + employeeName + ", category: " + category + ", content: " + content);
                    
                    results.add(new AiAnalysisResult(
                        employeeName,
                        "add_note",
                        content != null && !content.isEmpty() ? content : originalContent,
                        category != null && !category.isEmpty() ? category : "uncategorized",
                        confidence != null && !confidence.isEmpty() ? confidence : "medium",
                        item.toString()
                    ));
                }
            }

            return results;

        } catch (Exception e) {
            System.err.println("Failed to parse Gemini multiple response: " + e.getMessage());
            e.printStackTrace();
            return createMockMultipleResults(originalContent);
        }
    }

    private AiAnalysisResult createMockResult(String content) {
        // MockAIのロジックを使用
        MockAiAnalysisService mockService = new MockAiAnalysisService();
        return mockService.analyzeContent(content);
    }

    private List<AiAnalysisResult> createMockMultipleResults(String content) {
        // MockAIのロジックを使用
        MockAiAnalysisService mockService = new MockAiAnalysisService();
        return mockService.analyzeMultipleContent(content);
    }
} 