package com.tinypay.chat.dto;

public record CreateChatMessageRequest(
    String content,
    Long fileId   // 파일 첨부 시에만 포함 (선택)
) {
}
