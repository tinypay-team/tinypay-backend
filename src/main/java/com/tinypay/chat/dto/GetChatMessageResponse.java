package com.tinypay.chat.dto;

import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.request.dto.ApiItemResponse;
import com.tinypay.request.dto.AiRequestResponseStatus;
import com.tinypay.request.dto.GeneratedFileDto;

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
        List<GeneratedFileDto> generatedFiles,  // AI가 생성한 파일 (PDF, 이미지 등)
        Long fileId,        // 사용자가 첨부한 파일 (없으면 null)
        String fileName,
        String fileType,
        LocalDateTime createdAt
) {
}
