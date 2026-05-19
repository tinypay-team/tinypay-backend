package com.tinypay.chat.service;

import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.dto.CreateChatSessionResponse;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService {

    private static final String DEFAULT_TITLE = "새 채팅";

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateChatSessionResponse createChatSession(Long userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        ChatSession chatSession = ChatSession.builder()
                                      .user(user)
                                      .title(DEFAULT_TITLE)
                                      .build();

        ChatSession savedChatSession = chatSessionRepository.save(chatSession);

        return new CreateChatSessionResponse(savedChatSession.getId(), savedChatSession.getTitle(), savedChatSession.getCreatedAt()
        );
    }
}