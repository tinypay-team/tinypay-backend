package com.tinypay.chat.dto;

import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.request.dto.ApiItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetChatMessageResponse(
        Long messageId,
        SenderRole senderRole,
        MessageType messageType,
        String content,
        Long requestId,
        List<ApiItemResponse> apiItems,
        BigDecimal totalEstimatedCost,
        LocalDateTime createdAt
) {
}
