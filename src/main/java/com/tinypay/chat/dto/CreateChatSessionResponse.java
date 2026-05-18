package com.tinypay.chat.dto;

import java.time.LocalDateTime;

public record CreateChatSessionResponse(
    Long sessionId,
    String title,
    LocalDateTime createdAt
) {
}
