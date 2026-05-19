package com.tinypay.chat.dto;

import java.time.LocalDate;

public record CreateChatSessionResponse(
    Long sessionId,
    String title,
    LocalDate createdAt
) {
}
