package com.tinypay.request.dto;

import com.tinypay.dify.domain.AiRequestStatus;

public enum AiRequestResponseStatus {
    PENDING,
    WAITING_APPROVAL,
    COMPLETED,
    FAILED,
    CANCELLED;

    public static AiRequestResponseStatus from(AiRequestStatus status) {
        return switch (status) {
            case ANALYZING, APPROVED, EXECUTING -> PENDING;
            case WAITING_APPROVAL -> WAITING_APPROVAL;
            case COMPLETED -> COMPLETED;
            case FAILED -> FAILED;
            case CANCELLED -> CANCELLED;
        };
    }
}
