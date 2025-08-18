package com.example.kyarte.service;

import com.example.kyarte.dto.AiAnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@Primary // Gemini AIを有効化
public class GeminiAnalysisService implements AiAnalysisService {

    @Value("${gemini.api.key:dummy_key}")
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
            if (!isApiKeyConfigured()) {
                System.out.println("Gemini API key not configured or placeholder detected, falling back to mock analysis");
                return createMockResult(content);
            }

            String prompt = createAnalysisPrompt(content);
            String responseText = callGeminiApi(prompt);
            if (responseText == null || responseText.isBlank()) {
                System.err.println("Gemini API returned empty response, falling back to mock analysis");
                return createMockResult(content);
            }
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
            if (!isApiKeyConfigured()) {
                System.out.println("Gemini API key not configured or placeholder detected, falling back to mock analysis");
                return createMockMultipleResults(content);
            }

            String prompt = createMultipleAnalysisPrompt(content);
            String responseText = callGeminiApi(prompt);
            if (responseText == null || responseText.isBlank()) {
                System.err.println("Gemini API returned empty response, falling back to mock analysis");
                return createMockMultipleResults(content);
            }
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

    private boolean isApiKeyConfigured() {
        if (apiKey == null) {
            return false;
        }
        String trimmed = apiKey.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String lower = trimmed.toLowerCase();
        if ("dummy_key".equals(lower) || "your_api_key".equals(lower) || "your_api-key".equals(lower)) {
            return false;
        }
        return true;
    }

    private String createAnalysisPrompt(String content) {
        return """
            以下のテキストから従業員の情報を抽出してください。

            入力テキスト: %s

            以下のJSONオブジェクトのみを厳密に出力してください。JSON以外（説明文、マークダウン、バッククォートなど）は一切含めないでください：
            {
                "employeeName": "従業員名（姓のみ）",
                "action": "add_note",
                "content": "該当する部分の元テキスト",
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

            以下のJSON配列のみを厳密に出力してください。JSON以外（説明文、マークダウン、バッククォートなど）は一切含めないでください：
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
            String jsonText = extractTextFromCandidates(responseNode);
            
            System.out.println("Extracted text from response: " + jsonText);
            
            // ```json\n{...}\n``` の形式からJSONを抽出
            String jsonContent = extractJsonFromMarkdown(jsonText);
            System.out.println("Extracted JSON content: " + jsonContent);
            
            if (jsonContent == null || jsonContent.isBlank()) {
                System.err.println("Gemini response did not contain JSON text, falling back to mock");
                // 任意文字列から最初のJSONオブジェクトを抽出
                jsonContent = findFirstJson(jsonText, false);
                if (jsonContent == null || jsonContent.isBlank()) {
                    return createMockResult(originalContent);
                }
            }

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
            // ```json\n...\n``` や ```\n...\n``` の形式からJSONを抽出（言語ラベル任意/CRLF対応）
            Pattern pattern = Pattern.compile("```[a-zA-Z]*\\s*\\R?(.*?)\\R?```", Pattern.DOTALL);
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

    /**
     * candidates[].content.parts[].text を連結して返す。空なら従来の1件目にフォールバック。
     */
    private String extractTextFromCandidates(JsonNode responseNode) {
        try {
            JsonNode candidates = responseNode.path("candidates");
            if (candidates.isArray()) {
                StringBuilder combined = new StringBuilder();
                for (JsonNode cand : candidates) {
                    JsonNode parts = cand.path("content").path("parts");
                    if (parts.isArray()) {
                        for (JsonNode p : parts) {
                            String t = p.path("text").asText(null);
                            if (t != null) {
                                combined.append(t).append("\n");
                            }
                        }
                    }
                }
                String s = combined.toString().trim();
                if (!s.isEmpty()) {
                    return s;
                }
            }
        } catch (Exception ignore) {}
        return responseNode.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
    }

    /**
     * 任意テキストから最初のJSON片（配列/オブジェクト）を括弧バランスで抽出
     */
    private String findFirstJson(String text, boolean arrayPreferred) {
        if (text == null) return null;
        String trimmed = text.trim();
        String json = arrayPreferred ? extractBalanced(trimmed, '[', ']') : extractBalanced(trimmed, '{', '}');
        if (json == null || json.isBlank()) {
            json = arrayPreferred ? extractBalanced(trimmed, '{', '}') : extractBalanced(trimmed, '[', ']');
        }
        return json;
    }

    private String extractBalanced(String text, char open, char close) {
        int start = -1;
        int depth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == open) {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == close && depth > 0) {
                depth--;
                if (depth == 0 && start >= 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private List<AiAnalysisResult> parseGeminiMultipleResponse(String responseText, String originalContent) {
        try {
            List<AiAnalysisResult> results = new ArrayList<>();

            // まず、Gemini AIのレスポンス全体をJSONとして解析
            JsonNode responseNode = objectMapper.readTree(responseText);
            String jsonText = extractTextFromCandidates(responseNode);
            
            System.out.println("=== parseGeminiMultipleResponse Debug ===");
            System.out.println("Extracted text from response: " + jsonText);
            
            // ```json\n[...]\n``` の形式からJSONを抽出
            String jsonContent = extractJsonFromMarkdown(jsonText);
            System.out.println("Extracted JSON content: " + jsonContent);
            
            if (jsonContent == null || jsonContent.isBlank()) {
                System.err.println("Gemini response did not contain JSON array text, falling back to mock");
                // 任意文字列から最初のJSON配列を抽出。無ければオブジェクトを配列化
                jsonContent = findFirstJson(jsonText, true);
                if (jsonContent == null || jsonContent.isBlank()) {
                    return createMockMultipleResults(originalContent);
                }
            }

            // JSON配列を解析
            JsonNode jsonArray = objectMapper.readTree(jsonContent);
            if (!jsonArray.isArray()) {
                jsonArray = objectMapper.readTree("[" + jsonArray.toString() + "]");
            }
            
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

            if (results.isEmpty()) {
                // 解析できなかった場合は、元テキストを1件として返す
                results.add(new AiAnalysisResult(
                    null,
                    "add_note",
                    originalContent,
                    "uncategorized",
                    "low",
                    responseText
                ));
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