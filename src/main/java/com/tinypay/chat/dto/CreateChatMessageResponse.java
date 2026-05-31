package com.tinypay.chat.dto;

import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.dify.domain.AiRequestStatus;

import java.time.LocalDateTime;

public record CreateChatMessageResponse(
    Long messageId,
    Long sessionId,
    Long requestId,
    SenderRole senderRole,

    MessageType messageType,
    String content,
    AiRequestStatus requestStatus,
    LocalDateTime createdAt
) {
}
