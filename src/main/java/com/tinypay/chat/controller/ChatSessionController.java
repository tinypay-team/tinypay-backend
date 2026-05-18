package com.tinypay.chat.controller;

import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.dto.CreateChatSessionResponse;
import com.tinypay.chat.service.ChatSessionService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping
    public ApiResponse<CreateChatSessionResponse> createChatSession(@RequestParam Long userId) {
        return ApiResponse.success(SuccessType.CHAT_SESSION_CREATE_SUCCESS, chatSessionService.createChatSession(userId));
    }
}
