package com.tinypay.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConfirmUploadRequest(
        @NotBlank String fileName,
        @NotBlank String fileType,
        @Positive Long fileSize,
        @NotNull Long sessionId,
        @NotBlank String storageKey
) {}
