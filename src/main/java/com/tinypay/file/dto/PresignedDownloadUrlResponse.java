package com.tinypay.file.dto;

public record PresignedDownloadUrlResponse(
        String downloadUrl,
        String fileName,
        String fileType,
        Long fileSize,
        Long expiresIn
) {}
