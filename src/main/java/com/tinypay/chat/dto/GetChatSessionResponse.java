package com.tinypay.chat.dto;

import java.time.LocalDate;

public record GetChatSessionResponse(
    Long sessionId,
    String title,
    LocalDate createdAt
) {
}
