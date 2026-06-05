package com.tinypay.chat.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.domain.FileAttachment;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.dto.CreateChatMessageRequest;
import com.tinypay.chat.dto.CreateChatMessageResponse;
import com.tinypay.chat.dto.GetChatMessageResponse;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.chat.repository.FileAttachmentRepository;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestStatus;
import com.tinypay.dify.repository.AiRequestApiItemRepository;
import com.tinypay.dify.repository.AiRequestRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.request.dto.AiRequestResponseStatus;
import com.tinypay.request.dto.ApiItemResponse;
import com.tinypay.request.dto.GeneratedFileDto;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private static final int CONTEXT_MESSAGE_LIMIT = 10;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final AiRequestRepository aiRequestRepository;
    private final AiRequestApiItemRepository aiRequestApiItemRepository;
    private final ChatAnalysisService chatAnalysisService;
    private final DifyAsyncService difyAsyncService;
    private final UserRepository userRepository;

    @Transactional
    public CreateChatMessageResponse createChatMessage(Long userId, Long sessionId, CreateChatMessageRequest request) {

        // 1. 요청 검증 - content, fileId 둘 다 없으면 400
        if (request == null
                || ((request.content() == null || request.content().isBlank()) && request.fileId() == null)) {
            throw new CustomException(ErrorType.REQUEST_VALIDATION_EXCEPTION);
        }

        // 2. 세션 확인
        ChatSession chatSession = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorType.CHAT_SESSION_NOT_FOUND));

        // 3. context 수집 + 문자열 변환
        List<ChatMessage> recentMessages = getRecentMessagesForContext(sessionId);
        String contextString = chatAnalysisService.buildContextString(recentMessages);

        // 4. 사용자 메시지 저장
        String content = (request.content() != null && !request.content().isBlank())
                ? request.content() : null;
        MessageType messageType = (content == null && request.fileId() != null)
                ? MessageType.FILE : MessageType.TEXT;

        boolean isFirstMessage = chatMessageRepository.countBySessionId(sessionId) == 0;

        ChatMessage userMessage = chatMessageRepository.save(
                ChatMessage.builder()
                        .user(chatSession.getUser())
                        .session(chatSession)
                        .senderRole(SenderRole.USER)
                        .messageType(messageType)
                        .content(content)
                        .build()
        );

        if (isFirstMessage && content != null) {
            chatSession.updateTitle(generateChatTitle(content));
        }

        // 5. AiRequest 생성 (ANALYZING)
        String prompt = content != null ? content : "(파일 첨부)";
        AiRequest aiRequest = aiRequestRepository.save(
                AiRequest.builder()
                        .user(chatSession.getUser())
                        .session(chatSession)
                        .message(userMessage)
                        .prompt(prompt)
                        .status(AiRequestStatus.ANALYZING)
                        .build()
        );

        // userMessage ↔ aiRequest 양방향 연결
        userMessage.connectRequest(aiRequest);

        // 6. 파일 첨부가 있는 경우 메시지에 연결
        if (request.fileId() != null) {
            FileAttachment file = fileAttachmentRepository.findById(request.fileId())
                    .orElseThrow(() -> new CustomException(ErrorType.FILE_NOT_FOUND));
            file.connectMessage(userMessage);
        }

        chatMessageRepository.save(userMessage);

        // 7. 비동기 Dify 분석 트리거
        final Long aiRequestId = aiRequest.getId();
        final Long finalUserId = userId;
        final Long finalSessionId = sessionId;
        final String finalContent = prompt;
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

        // 8. ANALYZING 상태로 즉시 반환
        return new CreateChatMessageResponse(
                userMessage.getId(),
                chatSession.getId(),
                aiRequest.getId(),
                userMessage.getSenderRole(),
                userMessage.getMessageType(),
                userMessage.getContent(),
                AiRequestStatus.ANALYZING,
                userMessage.getCreatedAt()
        );
    }

    public List<GetChatMessageResponse> getChatMessages(Long userId, Long sessionId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorType.CHAT_SESSION_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);

        // requestId별 첫 번째 어시스턴트 메시지 ID 수집 → 결제카드 메시지 판별용
        Set<Long> paymentCardMessageIds = messages.stream()
                .filter(m -> m.getSenderRole() == SenderRole.ASSISTANT && m.getRequest() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        m -> m.getRequest().getId(),
                        java.util.stream.Collectors.minBy(java.util.Comparator.comparing(ChatMessage::getCreatedAt))
                ))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(opt -> opt.get().getId())
                .collect(java.util.stream.Collectors.toSet());

        return messages.stream()
                .map(message -> {
                    AiRequest request = message.getRequest();
                    List<ApiItemResponse> apiItems = null;
                    java.math.BigDecimal totalEstimatedCost = null;
                    AiRequestResponseStatus requestStatus = null;
                    List<GeneratedFileDto> generatedFiles = null;

                    if (request != null && message.getSenderRole() == SenderRole.ASSISTANT) {
                        requestStatus = AiRequestResponseStatus.from(request.getStatus());

                        boolean isPaymentCard = paymentCardMessageIds.contains(message.getId());

                        if (isPaymentCard) {
                            // 결제카드 메시지: apiItems + 예상금액만 표시
                            List<ApiItemResponse> fetchedItems = aiRequestApiItemRepository
                                    .findAllByRequestOrderByExecutionOrderAsc(request)
                                    .stream()
                                    .map(ApiItemResponse::from)
                                    .toList();
                            if (!fetchedItems.isEmpty()) {
                                apiItems = fetchedItems;
                                totalEstimatedCost = request.getEstimatedTotalCost();
                            }
                        } else {
                            // 결과 메시지: generatedFiles만 표시
                            if (request.getStatus() == AiRequestStatus.COMPLETED) {
                                generatedFiles = parseJson(request.getGeneratedFiles(), new TypeReference<>() {});
                            }
                        }
                    }

                    // 사용자가 첨부한 파일 조회
                    FileAttachment file = fileAttachmentRepository
                            .findByMessage_Id(message.getId())
                            .orElse(null);

                    return new GetChatMessageResponse(
                            message.getId(),
                            message.getSenderRole(),
                            message.getMessageType(),
                            message.getContent(),
                            request != null ? request.getId() : null,
                            requestStatus,
                            apiItems,
                            totalEstimatedCost,
                            generatedFiles,
                            file != null ? file.getId() : null,
                            file != null ? file.getFileName() : null,
                            file != null ? file.getFileType() : null,
                            message.getCreatedAt()
                    );
                })
                .toList();
    }

    private <T> List<T> parseJson(String json, TypeReference<List<T>> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            List<T> result = objectMapper.readValue(json, typeRef);
            return (result == null || result.isEmpty()) ? null : result;
        } catch (Exception e) {
            log.warn("[ChatMessageService] JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private List<ChatMessage> getRecentMessagesForContext(Long sessionId) {
        List<ChatMessage> all = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (all.size() <= CONTEXT_MESSAGE_LIMIT) return all;
        return all.subList(all.size() - CONTEXT_MESSAGE_LIMIT, all.size());
    }

    private static final int MAX_TITLE_LENGTH = 30;

    private String generateChatTitle(String content) {
        String trimmed = content.strip();
        if (trimmed.length() <= MAX_TITLE_LENGTH) return trimmed;
        int cut = trimmed.lastIndexOf(' ', MAX_TITLE_LENGTH);
        if (cut <= 0) cut = MAX_TITLE_LENGTH;
        return trimmed.substring(0, cut) + "...";
    }
}
