package com.tinypay.chat.service;

import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.dify.client.DifyClient;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestApiItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinypay.dify.dto.ServiceExecutionRequest;
import com.tinypay.dify.dto.ServiceExecutionResponse;
import com.tinypay.request.dto.ExecutedServiceDto;
import com.tinypay.request.dto.GeneratedFileDto;
import com.tinypay.dify.repository.AiRequestApiItemRepository;
import com.tinypay.dify.repository.AiRequestRepository;
import com.tinypay.finance.domain.PaymentLog;
import com.tinypay.finance.domain.PaymentStatus;
import com.tinypay.finance.repository.PaymentLogRepository;
import com.tinypay.file.service.S3Service;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 결제 완료 후 Dify 서비스 실행 Workflow를 비동기로 처리하는 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyServiceExecutionService {

    private static final int CONTEXT_MESSAGE_LIMIT = 10;

    private final AiRequestRepository aiRequestRepository;
    private final AiRequestApiItemRepository aiRequestApiItemRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatAnalysisService chatAnalysisService;
    private final DifyClient difyClient;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async("difyTaskExecutor")
    @Transactional
    public void executeService(Long aiRequestId) {
        log.info("[DifyServiceExecution] 서비스 실행 시작: aiRequestId={}", aiRequestId);

        AiRequest aiRequest = aiRequestRepository.findById(aiRequestId)
                .orElseThrow(() -> new CustomException(ErrorType.AI_REQUEST_NOT_FOUND));

        ChatSession chatSession = aiRequest.getSession();

        try {
            // 1. 결제 정보 조회
            PaymentLog paymentLog = paymentLogRepository.findByRequest(aiRequest)
                    .orElseThrow(() -> new CustomException(ErrorType.INTERNAL_SERVER_ERROR));

            // 2. 승인된 서비스 목록 조회
            List<AiRequestApiItem> apiItems = aiRequestApiItemRepository
                    .findAllByRequestOrderByExecutionOrderAsc(aiRequest);

            // 3. Dify 요청 빌드
            ServiceExecutionRequest.PaymentResult paymentResult = new ServiceExecutionRequest.PaymentResult(
                    paymentLog.getId(),
                    paymentLog.getPaymentStatus() == PaymentStatus.SUCCESS,
                    paymentLog.getAmount(),
                    "USDC",
                    paymentLog.getTxHash(),
                    paymentLog.getPaymentStatus().name()
            );

            List<ServiceExecutionRequest.ApprovedService> approvedServices = apiItems.stream()
                    .map(item -> new ServiceExecutionRequest.ApprovedService(
                            item.getApiName(),
                            item.getProviderName(),
                            item.getDescription(),
                            item.getEstimatedCost(),
                            "USDC",
                            item.getOutputType() != null ? item.getOutputType() : "TEXT"
                    ))
                    .toList();

            // 4. context 빌드 (최근 N개 메시지)
            List<ChatMessage> recentMessages = getRecentMessagesForContext(chatSession.getId());
            String contextString = chatAnalysisService.buildContextString(recentMessages);

            ServiceExecutionRequest request = ServiceExecutionRequest.of(
                    aiRequest.getUser().getId(),
                    chatSession.getId(),
                    aiRequest.getPrompt(),
                    contextString,
                    paymentResult,
                    approvedServices
            );

            // 5. Dify 서비스 실행 Workflow 호출
            ServiceExecutionResponse response = difyClient.runServiceExecution(request);

            // 6. 결과 ASSISTANT 메시지 저장
            String answer = (response.data() != null && response.data().answer() != null)
                    ? response.data().answer()
                    : "(결과를 가져오지 못했습니다.)";

            chatMessageRepository.save(
                    ChatMessage.builder()
                            .session(chatSession)
                            .request(aiRequest)
                            .senderRole(SenderRole.ASSISTANT)
                            .messageType(MessageType.TEXT)
                            .content(answer)
                            .build()
            );

            // 7. Dify 임시 URL → S3 영구 저장 (expires_at 만료 대비)
            List<GeneratedFileDto> generatedFiles = response.data() != null
                    ? response.data().generatedFiles() : null;
            List<GeneratedFileDto> s3Files = uploadGeneratedFilesToS3(
                    generatedFiles, aiRequest.getUser().getId(), aiRequestId);

            // 8. AiRequest 완료 처리 (executed_services, generated_files 저장)
            String executedServicesJson = toJson(
                    response.data() != null ? response.data().executedServices() : null);
            String generatedFilesJson = toJson(s3Files);
            String resultTitle = response.data() != null ? response.data().resultTitle() : null;
            aiRequest.completeWithResult(resultTitle, executedServicesJson, generatedFilesJson);

            log.info("[DifyServiceExecution] 서비스 실행 완료: aiRequestId={}", aiRequestId);

        } catch (CustomException e) {
            aiRequest.fail("서비스 실행 오류: " + e.getMessage());
            saveErrorMessage(chatSession, aiRequest, "서비스 실행 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            log.error("[DifyServiceExecution] 처리 실패: aiRequestId={}, error={}", aiRequestId, e.getMessage());

        } catch (Exception e) {
            aiRequest.fail("예기치 않은 오류: " + e.getMessage());
            saveErrorMessage(chatSession, aiRequest, "서비스 실행 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            log.error("[DifyServiceExecution] 예기치 않은 오류: aiRequestId={}", aiRequestId, e);
        }
    }

    // Dify 임시 URL 파일들을 S3에 업로드하고 URL 교체
    private List<GeneratedFileDto> uploadGeneratedFilesToS3(
            List<GeneratedFileDto> files, Long userId, Long aiRequestId) {
        if (files == null || files.isEmpty()) return files;

        return files.stream().map(file -> {
            if (file.fileUrl() == null || file.fileUrl().isBlank()) return file;

            String s3Key = s3Service.buildS3Key(userId, aiRequestId, file.fileName());
            String contentType = switch (file.fileType() != null ? file.fileType().toUpperCase() : "") {
                case "IMAGE" -> "image/png";
                case "PDF"   -> "application/pdf";
                default      -> "application/octet-stream";
            };
            String s3Url = s3Service.uploadFromUrl(file.fileUrl(), s3Key, contentType);

            return new GeneratedFileDto(file.fileName(), s3Url, file.fileType(), file.mimeType());
        }).toList();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("[DifyServiceExecution] JSON 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }

    private List<ChatMessage> getRecentMessagesForContext(Long sessionId) {
        List<ChatMessage> all = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (all.size() <= CONTEXT_MESSAGE_LIMIT) return all;
        return all.subList(all.size() - CONTEXT_MESSAGE_LIMIT, all.size());
    }

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
            log.error("[DifyServiceExecution] 오류 메시지 저장 실패", e);
        }
    }
}
