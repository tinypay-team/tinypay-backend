package com.tinypay.chat.dto;

import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.request.dto.ApiItemResponse;
import com.tinypay.request.dto.AiRequestResponseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetChatMessageResponse(
        Long messageId,
        SenderRole senderRole,
        MessageType messageType,
        String content,
        Long requestId,
        AiRequestResponseStatus requestStatus,
        List<ApiItemResponse> apiItems,
        BigDecimal totalEstimatedCost,
        Long fileId,        // 첨부파일 있을 때만 포함 (없으면 null)
        String fileName,    // 첨부파일 이름 (없으면 null)
        String fileType,    // 첨부파일 MIME 타입 (없으면 null)
        LocalDateTime createdAt
) {
}
