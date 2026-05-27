package com.tinypay.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Dify 응답의 required_services 항목
 *
 * 명세서 필드:
 * - service_name    : 서비스명
 * - service_type    : 서비스 유형 (API, AGENT, FILE_GENERATOR, ...)
 * - service_purpose : 해당 서비스 사용 목적
 * - estimated_cost  : 예상 비용
 * - currency        : 결제 통화 (USDC)
 * - risk_level      : 위험도 (LOW, MEDIUM, HIGH)
 * - output_type     : 결과물 유형 (TEXT, IMAGE, PDF, ...)
 * - reason          : 해당 서비스가 필요한 이유
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RequiredServiceDto(

        @JsonProperty("service_name")
        String serviceName,

        @JsonProperty("service_type")
        String serviceType,

        @JsonProperty("service_purpose")
        String servicePurpose,

        @JsonProperty("estimated_cost")
        BigDecimal estimatedCost,

        String currency,

        @JsonProperty("risk_level")
        String riskLevel,

        @JsonProperty("output_type")
        String outputType,

        String reason
) {
}
