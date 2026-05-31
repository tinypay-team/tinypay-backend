package com.tinypay.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PresignedUploadUrlRequest(
        @NotBlank String fileName,
        @NotBlank String fileType,
        @Positive Long fileSize,
        Long sessionId
) {}
