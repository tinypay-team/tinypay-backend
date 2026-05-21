package com.tinypay.chat.service;

import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.dto.CreateChatMessageRequest;
import com.tinypay.chat.dto.CreateChatMessageResponse;
import com.tinypay.chat.repository.AiRequestRepository;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.request.domain.AiRequest;
import com.tinypay.request.domain.AiRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final AiRequestRepository aiRequestRepository;

    @Transactional
    public CreateChatMessageResponse createChatMessage(Long userId, CreateChatMessageRequest request) {
        // request 검증
        if (request == null
                || request.sessionId() == null
                || request.content() == null
                || request.content().isBlank()) {
            throw new CustomException(ErrorType.REQUEST_VALIDATION_EXCEPTION);
        }

        ChatSession chatSession = chatSessionRepository.findByIdAndUserId(request.sessionId(), userId)
                                      .orElseThrow(() -> new CustomException(ErrorType.CHAT_SESSION_NOT_FOUND));

        ChatMessage chatMessage = ChatMessage.builder()
                                      .user(chatSession.getUser())
                                      .session(chatSession)
                                      .request(null)
                                      .senderRole(SenderRole.USER)
                                      .messageType(MessageType.TEXT)
                                      .content(request.content())
                                      .build();

        ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        AiRequest aiRequest = AiRequest.builder()
                                  .user(chatSession.getUser())
                                  .session(chatSession)
                                  .message(savedChatMessage)
                                  .prompt(request.content())
                                  .status(AiRequestStatus.ANALYZING)
                                  .build();

        AiRequest savedAiRequest = aiRequestRepository.save(aiRequest);

        // 메시지와 request 연결
        savedChatMessage.connectRequest(savedAiRequest);
        chatMessageRepository.save(savedChatMessage);

        return new CreateChatMessageResponse(savedChatMessage.getId(), chatSession.getId(), savedAiRequest.getId(), savedChatMessage.getSenderRole(), savedChatMessage.getMessageType(), savedChatMessage.getContent(), savedAiRequest.getStatus(), savedChatMessage.getCreatedAt()
        );
    }
}
