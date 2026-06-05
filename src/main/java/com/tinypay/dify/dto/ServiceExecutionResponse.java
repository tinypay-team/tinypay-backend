package com.tinypay.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tinypay.dify.dto.DifyListDeserializer;
import com.tinypay.request.dto.ExecutedServiceDto;
import com.tinypay.request.dto.GeneratedFileDto;

import java.util.List;

// Dify 결제 완료 후 서비스 실행 Workflow 응답 DTO
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServiceExecutionResponse(

        Integer code,
        String message,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(

            @JsonProperty("response_type")
            String responseType,

            @JsonProperty("output_type")
            String outputType,

            @JsonProperty("result_title")
            String resultTitle,

            String answer,

            String reason,

            @JsonProperty("executed_services")
            @JsonDeserialize(using = DifyListDeserializer.ExecutedServiceList.class)
            List<ExecutedServiceDto> executedServices,

            @JsonProperty("generated_files")
            @JsonDeserialize(using = DifyListDeserializer.GeneratedFileList.class)
            List<GeneratedFileDto> generatedFiles
    ) {}

    public boolean isSuccess() {
        return code != null && code == 200 && data != null;
    }
}
