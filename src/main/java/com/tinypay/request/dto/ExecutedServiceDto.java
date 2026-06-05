package com.tinypay.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Dify 서비스 실행 결과 - 실제로 실행된 서비스 항목
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExecutedServiceDto(

        @JsonProperty("service_name")
        String serviceName,

        @JsonProperty("service_type")
        String serviceType,

        boolean success,

        @JsonProperty("error_message")
        String errorMessage
) {
}
