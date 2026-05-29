package com.tinypay.chat.service;

import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.dify.dto.ChatAnalysisResponse;
import com.tinypay.dify.dto.ChatAnalysisResponse.Data;
import com.tinypay.dify.dto.RequiredServiceDto;
import com.tinypay.dify.repository.AiRequestApiItemRepository;
import com.tinypay.dify.repository.AiRequestRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestApiItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Dify 분석을 백그라운드에서 비동기로 처리하는 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyAsyncService {

    private final ChatAnalysisService chatAnalysisService;
    private final AiRequestRepository aiRequestRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiRequestApiItemRepository aiRequestApiItemRepository;

    @Async("difyTaskExecutor")
    @Transactional
    public void processAnalysis(
            Long aiRequestId,
            Long userId,
            Long sessionId,
            String message,
            String contextString
    ) {
        log.info("[DifyAsync] 분석 시작: aiRequestId={}", aiRequestId);

        // 1. AiRequest 재조회 (새 스레드는 새 트랜잭션이므로 DB에서 다시 로딩)
        AiRequest aiRequest = aiRequestRepository.findById(aiRequestId)
                .orElseThrow(() -> new CustomException(ErrorType.AI_REQUEST_NOT_FOUND));

        ChatSession chatSession = aiRequest.getSession();

        try {
            // 2. Dify 호출 (이미 조합된 context 문자열 사용)
            ChatAnalysisResponse difyResponse = chatAnalysisService.analyzeWithContext(
                    userId, sessionId, message, contextString
            );

            // 3. response_type 분기
            handleDifyResponse(difyResponse, aiRequest, chatSession);

        } catch (CustomException e) {
            // Dify 호출 실패 또는 응답 오류
            aiRequest.fail("Dify 오류: " + e.getMessage());
            saveErrorMessage(chatSession, aiRequest, "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            log.error("[DifyAsync] 처리 실패: aiRequestId={}, error={}", aiRequestId, e.getMessage());

        } catch (Exception e) {
            // 예상치 못한 오류
            aiRequest.fail("예기치 않은 오류: " + e.getMessage());
            saveErrorMessage(chatSession, aiRequest, "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            log.error("[DifyAsync] 예기치 않은 오류: aiRequestId={}", aiRequestId, e);
        }
    }

    private void handleDifyResponse(
            ChatAnalysisResponse difyResponse,
            AiRequest aiRequest,
            ChatSession chatSession
    ) {
        Data data = difyResponse.data();
        if (data == null) {
            throw new CustomException(ErrorType.DIFY_RESPONSE_INVALID);
        }

        String responseType = data.responseType();
        log.info("[DifyAsync] responseType={}, aiRequestId={}", responseType, aiRequest.getId());

        switch (responseType) {
            case "ANSWER"              -> handleAnswer(data, aiRequest, chatSession);
            case "PAYMENT_REQUIRED"    -> handlePaymentRequired(data, aiRequest, chatSession);
            case "UNSUPPORTED_REQUEST" -> handleUnsupportedRequest(data, aiRequest, chatSession);
            default -> throw new CustomException(ErrorType.DIFY_RESPONSE_INVALID);  // fail()은 상위 catch에서 처리
        }
    }

    // ANSWER: AI가 바로 답변 가능한 경우
    private void handleAnswer(Data data, AiRequest aiRequest, ChatSession chatSession) {
        String answer = data.answer() != null ? data.answer() : "(응답 없음)";

        chatMessageRepository.save(
                ChatMessage.builder()
                        .session(chatSession)
                        .request(aiRequest)
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TEXT)
                        .content(answer)
                        .build()
        );

        aiRequest.complete();
        log.info("[DifyAsync] ANSWER 완료: aiRequestId={}", aiRequest.getId());
    }

    // PAYMENT_REQUIRED: 유료 서비스 필요
    private void handlePaymentRequired(Data data, AiRequest aiRequest, ChatSession chatSession) {
        BigDecimal totalCost = data.totalEstimatedCost() != null ? data.totalEstimatedCost() : BigDecimal.ZERO;
        String confirmationMsg = data.userConfirmationMessage() != null
                ? data.userConfirmationMessage() : "결제 승인이 필요합니다.";

        aiRequest.updateEstimatedResult(totalCost, buildAnalysisSummary(data));

        saveApiItems(aiRequest, data.requiredServices());

        chatMessageRepository.save(
                ChatMessage.builder()
                        .session(chatSession)
                        .request(aiRequest)
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TEXT)
                        .content(confirmationMsg)
                        .build()
        );

        log.info("[DifyAsync] PAYMENT_REQUIRED 완료: aiRequestId={}, cost={}", aiRequest.getId(), totalCost);
    }

    // UNSUPPORTED_REQUEST: 현재 지원 불가 요청
    private void handleUnsupportedRequest(Data data, AiRequest aiRequest, ChatSession chatSession) {

        String guideMsg = data.answer() != null ? data.answer()
                : (data.unsupportedReason() != null ? data.unsupportedReason() : "지원하지 않는 요청입니다.");

        aiRequest.fail(data.unsupportedReason());

        chatMessageRepository.save(
                ChatMessage.builder()
                        .session(chatSession)
                        .request(aiRequest)
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TEXT)
                        .content(guideMsg)
                        .build()
        );

        log.info("[DifyAsync] UNSUPPORTED_REQUEST 완료: aiRequestId={}", aiRequest.getId());
    }

    // 오류 발생 시 사용자에게 보여줄 안내 메시지 저장
    private void saveErrorMessage(ChatSession chatSession, AiRequest aiRequest, String content) {
        try {
            chatMessageRepository.save(
                    ChatMessage.builder()
                            .session(chatSession)
                            .request(aiRequest)
                            .senderRole(SenderRole.ASSISTANT)
                            .messageType(MessageType.TEXT)
                            .content(content)
                            .build()
            );
        } catch (Exception e) {
            log.error("[DifyAsync] 오류 메시지 저장 실패", e);
        }
    }

    // RequiredServiceDto → AiRequestApiItem DB 저장
    private void saveApiItems(AiRequest aiRequest, List<RequiredServiceDto> services) {
        if (services == null || services.isEmpty()) return;

        for (int i = 0; i < services.size(); i++) {
            RequiredServiceDto s = services.get(i);
            aiRequestApiItemRepository.save(
                    AiRequestApiItem.builder()
                            .request(aiRequest)
                            .apiName(s.serviceName())
                            .providerName(s.serviceType())
                            .description(s.servicePurpose())
                            .estimatedCost(s.estimatedCost() != null ? s.estimatedCost() : BigDecimal.ZERO)
                            .executionOrder(i + 1)
                            .build()
            );
        }
    }

    // 분석 요약 문자열
    private String buildAnalysisSummary(Data data) {
        if (data.requiredServices() == null) return "";
        StringBuilder sb = new StringBuilder();
        data.requiredServices().forEach(s ->
                sb.append(s.serviceName()).append("(").append(s.estimatedCost()).append(" USDC), ")
        );
        return sb.toString().replaceAll(", $", "");
    }
}
