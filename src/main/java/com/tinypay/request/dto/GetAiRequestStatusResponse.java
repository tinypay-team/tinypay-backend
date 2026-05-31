package com.tinypay.request.dto;

public record GetAiRequestStatusResponse(
        Long requestId,
        Long sessionId,
        AiRequestResponseStatus requestStatus
) {
}
