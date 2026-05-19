package com.tinypay.chat.controller;

import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.dto.CreateChatSessionResponse;
import com.tinypay.chat.service.ChatSessionService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateChatSessionResponse>> createChatSession(@RequestParam Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(SuccessType.CREATE_CHAT_SESSION_SUCCESS, chatSessionService.createChatSession(userId)));
    }
}
