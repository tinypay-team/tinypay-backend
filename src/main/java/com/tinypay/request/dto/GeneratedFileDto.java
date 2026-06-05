package com.tinypay.request.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Dify 서비스 실행 결과 - 생성된 파일 항목
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeneratedFileDto(

        @JsonAlias("file_name")
        String fileName,

        @JsonAlias("file_url")
        String fileUrl,

        @JsonAlias("file_type")
        String fileType,    // "IMAGE" | "PDF"

        @JsonAlias("mime_type")
        String mimeType     // "image/png", "application/pdf"
) {
}
