package com.tinypay.chat.service;

import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.dto.CreateChatMessageRequest;
import com.tinypay.chat.dto.CreateChatMessageResponse;
import com.tinypay.chat.dto.GetChatMessageResponse;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.dify.repository.AiRequestApiItemRepository;
import com.tinypay.dify.repository.AiRequestRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.request.dto.ApiItemResponse;
import com.tinypay.user.repository.UserRepository;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final int CONTEXT_MESSAGE_LIMIT = 10;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final AiRequestRepository aiRequestRepository;
    private final AiRequestApiItemRepository aiRequestApiItemRepository;
    private final ChatAnalysisService chatAnalysisService;
    private final DifyAsyncService difyAsyncService;
    private final UserRepository userRepository;

    @Transactional
    public CreateChatMessageResponse createChatMessage(Long userId, Long sessionId, CreateChatMessageRequest request) {

        // 1. 요청 검증
        if (request == null
                || request.content() == null
                || request.content().isBlank()) {
            throw new CustomException(ErrorType.REQUEST_VALIDATION_EXCEPTION);
        }

        // 2. 세션 확인
        ChatSession chatSession = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorType.CHAT_SESSION_NOT_FOUND));

        // 3. context 수집 + 문자열 변환
        List<ChatMessage> recentMessages = getRecentMessagesForContext(sessionId);
        String contextString = chatAnalysisService.buildContextString(recentMessages);

        // 4. 사용자 메시지 저장
        ChatMessage userMessage = chatMessageRepository.save(
                ChatMessage.builder()
                        .user(chatSession.getUser())
                        .session(chatSession)
                        .senderRole(SenderRole.USER)
                        .messageType(MessageType.TEXT)
                        .content(request.content())
                        .build()
        );

        // 5. AiRequest 생성 (ANALYZING)
        AiRequest aiRequest = aiRequestRepository.save(
                AiRequest.builder()
                        .user(chatSession.getUser())
                        .session(chatSession)
                        .message(userMessage)
                        .prompt(request.content())
                        .status(AiRequestStatus.ANALYZING)
                        .build()
        );

        // userMessage ↔ aiRequest 양방향 연결
        userMessage.connectRequest(aiRequest);
        chatMessageRepository.save(userMessage);

        // 6. 비동기 Dify 분석 트리거
        final Long aiRequestId = aiRequest.getId();
        final Long finalUserId = userId;
        final Long finalSessionId = sessionId;
        final String finalContent = request.content();
        final String finalContext = contextString;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                difyAsyncService.processAnalysis(
                        aiRequestId, finalUserId, finalSessionId, finalContent, finalContext
                );
            }
        });

        log.info("[ChatMessageService] 메시지 저장 완료, 비동기 분석 트리거: messageId={}, requestId={}",
                userMessage.getId(), aiRequest.getId());

        // 7. ANALYZING 상태로 즉시 반환
        return new CreateChatMessageResponse(
                userMessage.getId(),
                chatSession.getId(),
                aiRequest.getId(),
                userMessage.getSenderRole(),
                userMessage.getMessageType(),
                userMessage.getContent(),
                AiRequestStatus.ANALYZING,   // 항상 ANALYZING
                userMessage.getCreatedAt()
        );
    }


    public List<GetChatMessageResponse> getChatMessages(Long userId, Long sessionId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorType.CHAT_SESSION_NOT_FOUND));

        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(message -> {
                    AiRequest request = message.getRequest();
                    List<ApiItemResponse> apiItems = null;
                    java.math.BigDecimal totalEstimatedCost = null;

                    if (request != null
                            && message.getSenderRole() == SenderRole.ASSISTANT
                            && request.getStatus() == AiRequestStatus.WAITING_APPROVAL) {
                        apiItems = aiRequestApiItemRepository.findAllByRequestOrderByExecutionOrderAsc(request)
                                .stream()
                                .map(ApiItemResponse::from)
                                .toList();
                        totalEstimatedCost = request.getEstimatedTotalCost();
                    }

                    return new GetChatMessageResponse(
                            message.getId(),
                            message.getSenderRole(),
                            message.getMessageType(),
                            message.getContent(),
                            request != null ? request.getId() : null,
                            apiItems,
                            totalEstimatedCost,
                            message.getCreatedAt()
                    );
                })
                .toList();
    }

    // context용 이전 메시지 최근 N개 조회
    private List<ChatMessage> getRecentMessagesForContext(Long sessionId) {
        List<ChatMessage> all = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (all.size() <= CONTEXT_MESSAGE_LIMIT) return all;
        return all.subList(all.size() - CONTEXT_MESSAGE_LIMIT, all.size());
    }
}
