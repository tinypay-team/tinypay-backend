package com.tinypay.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChatMessageRequest(
    String content
) {
}
