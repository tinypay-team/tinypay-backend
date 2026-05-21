package com.tinypay.chat.controller;

import com.tinypay.chat.dto.CreateChatMessageRequest;
import com.tinypay.chat.dto.CreateChatMessageResponse;
import com.tinypay.chat.service.ChatMessageService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChatMessageResponse>> createChatMessage(@RequestParam Long userId, @RequestBody CreateChatMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(SuccessType.CHAT_MESSAGE_CREATE_SUCCESS, chatMessageService.createChatMessage(userId, request)));
    }
}
