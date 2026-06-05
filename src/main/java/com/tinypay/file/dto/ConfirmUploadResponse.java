package com.tinypay.file.dto;

public record ConfirmUploadResponse(
        Long fileId,
        String fileName,
        String fileType,
        Long fileSize,
        String storageKey
) {}
