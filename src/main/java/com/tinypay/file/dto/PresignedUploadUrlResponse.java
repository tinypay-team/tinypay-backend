package com.tinypay.file.dto;

public record PresignedUploadUrlResponse(
        String uploadUrl,
        String storageKey,
        String fileUrl,
        Long expiresIn
) {}
