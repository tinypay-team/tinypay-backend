package com.tinypay.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Dify 결제 완료 후 서비스 실행 Workflow 요청 DTO
public record ServiceExecutionRequest(

        Map<String, Object> inputs,

        @JsonProperty("response_mode")
        String responseMode,

        String user
) {
    public static ServiceExecutionRequest of(
            Long userId,
            Long sessionId,
            String originalPrompt,
            String context,
            PaymentResult paymentResult,
            List<ApprovedService> approvedServices
    ) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("user_id", userId);
        inputs.put("chat_room_id", sessionId);
        inputs.put("session_id", String.valueOf(sessionId));
        inputs.put("original_prompt", originalPrompt != null ? originalPrompt : "");
        inputs.put("context", context != null ? context : "");
        inputs.put("payment_result", paymentResult);
        // Dify는 approved_services를 JSON 문자열(json.dumps) 형식으로 요구
        try {
            inputs.put("approved_services", new ObjectMapper().writeValueAsString(approvedServices));
        } catch (JsonProcessingException e) {
            inputs.put("approved_services", "[]");
        }

        return new ServiceExecutionRequest(inputs, "blocking", String.valueOf(userId));
    }

    public record PaymentResult(
            @JsonProperty("payment_id") Long paymentId,
            boolean paid,
            @JsonProperty("total_paid_amount") BigDecimal totalPaidAmount,
            String currency,
            @JsonProperty("transaction_hash") String transactionHash,
            @JsonProperty("payment_status") String paymentStatus
    ) {}

    public record ApprovedService(
            @JsonProperty("service_name") String serviceName,
            @JsonProperty("service_type") String serviceType,
            @JsonProperty("service_purpose") String servicePurpose,
            @JsonProperty("estimated_cost") BigDecimal estimatedCost,
            String currency,
            @JsonProperty("output_type") String outputType
    ) {}
}
