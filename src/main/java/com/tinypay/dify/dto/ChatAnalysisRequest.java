package com.tinypay.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

// Dify 채팅 요청 분석 Workflow 요청 DTO
public record ChatAnalysisRequest(

        Map<String, Object> inputs,

        @JsonProperty("response_mode")
        String responseMode,

        String user
) {
    public static ChatAnalysisRequest of(Long userId, Long sessionId, String message, String context) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("user_id", userId);
        inputs.put("chat_room_id", sessionId);
        inputs.put("message", message);
        inputs.put("session_id", String.valueOf(sessionId));
        inputs.put("context", context != null ? context : "");
        inputs.put("default_currency", "USDC");

        return new ChatAnalysisRequest(inputs, "blocking", String.valueOf(userId));
    }
}