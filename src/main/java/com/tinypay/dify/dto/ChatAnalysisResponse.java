package com.tinypay.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

// Dify 채팅 요청 분석 Workflow 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatAnalysisResponse(

        Integer code,
        String message,
        Data data
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(

            @JsonProperty("workflow_run_id")
            String workflowRunId,

            @JsonProperty("task_id")
            String taskId,

            @JsonProperty("response_type")
            String responseType,

            @JsonProperty("output_type")
            String outputType,

            // ANSWER 케이스 + UNSUPPORTED_REQUEST 케이스
            String answer,

            @JsonProperty("requires_paid_service")
            Boolean requiresPaidService,

            @JsonProperty("requires_agent")
            Boolean requiresAgent,

            // PAYMENT_REQUIRED 케이스
            @JsonProperty("required_services")
            List<RequiredServiceDto> requiredServices,

            @JsonProperty("total_estimated_cost")
            BigDecimal totalEstimatedCost,

            String currency,

            @JsonProperty("risk_level")
            String riskLevel,

            @JsonProperty("user_confirmation_message")
            String userConfirmationMessage,

            // UNSUPPORTED_REQUEST 케이스
            @JsonProperty("unsupported_reason")
            String unsupportedReason,

            @JsonProperty("missing_capabilities")
            List<String> missingCapabilities,

            @JsonProperty("suggested_alternatives")
            List<String> suggestedAlternatives,

            String reason
    ) {
    }

    public boolean isSuccess() {
        return code != null && code == 200 && data != null;
    }

    public String getResponseType() {
        return data != null ? data.responseType() : null;
    }
}
