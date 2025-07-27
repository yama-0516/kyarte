package com.example.kyarte.service;

import com.example.kyarte.dto.AiAnalysisResult;
import java.util.List;

public interface AiAnalysisService {
    
    /**
     * テキスト内容をAIで解析し、従業員データへの振り分け結果を返す
     * @param content 解析対象のテキスト
     * @return AI解析結果
     */
    AiAnalysisResult analyzeContent(String content);
    
    /**
     * 複数の情報を全て処理するメソッド
     * @param content 解析対象のテキスト
     * @return AI解析結果のリスト
     */
    List<AiAnalysisResult> analyzeMultipleContent(String content);
    
    /**
     * 使用するAIサービス名を返す
     * @return サービス名（"Gemini" または "OpenAI"）
     */
    String getServiceName();
} 