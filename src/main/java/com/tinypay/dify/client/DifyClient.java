package com.tinypay.dify.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinypay.abuse.domain.AbuseActionType;
import com.tinypay.abuse.domain.AbuseType;
import com.tinypay.abuse.service.AbuseService;
import com.tinypay.dify.config.DifyProperties;
import com.tinypay.dify.dto.ChatAnalysisRequest;
import com.tinypay.dify.dto.ChatAnalysisResponse;
import com.tinypay.dify.dto.DifyWorkflowResponse;
import com.tinypay.dify.dto.ServiceExecutionRequest;
import com.tinypay.dify.dto.ServiceExecutionResponse;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.security.validation.DifyResponseValidator;
import com.tinypay.security.validation.ValidationResult;
import com.tinypay.security.validation.ValidationSeverity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DifyClient {

    private static final String WORKFLOW_RUN_PATH = "/workflows/run";

    private final DifyProperties difyProperties;
    private final RestClient restClient;
    private final DifyResponseValidator difyResponseValidator;
    private final AbuseService abuseService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public ChatAnalysisResponse runChatAnalysis(ChatAnalysisRequest request) {
        String url = difyProperties.baseUrl() + WORKFLOW_RUN_PATH;
        log.debug("[DifyClient] 채팅 분석 요청 → {} | user={}", url, request.user());

        try {
            String rawBody = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + difyProperties.chatAnalysisApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            DifyWorkflowResponse raw = objectMapper.readValue(rawBody, DifyWorkflowResponse.class);

            if (raw == null || !raw.isSucceeded()) {
                String errDetail = (raw != null && raw.data() != null) ? raw.data().error() : "null response";
                log.error("[DifyClient] 워크플로우 실패: {}", errDetail);
                throw new CustomException(ErrorType.DIFY_API_ERROR);
            }

            ChatAnalysisResponse.Data outputs = raw.getOutputs();
            if (outputs == null || outputs.responseType() == null) {
                log.error("[DifyClient] outputs 또는 response_type 없음");
                throw new CustomException(ErrorType.DIFY_RESPONSE_INVALID);
            }

            log.info("[DifyClient] 분석 완료: responseType={}", outputs.responseType());

            // ===== 보안: Dify 응답 검증 =====
            ChatAnalysisResponse analysisResponse = new ChatAnalysisResponse(200, "success", outputs);
            ValidationResult validation = difyResponseValidator.validate(analysisResponse);

            if (!validation.isSafe()) {
                log.warn("[DifyClient] 응답 검증 실패: severity={}, rules={}, reason={}",
                        validation.getSeverity(),
                        validation.getMatchedRules(),
                        validation.getReason());

                // HIGH/CRITICAL은 어뷰징 기록 + 차단
                if (validation.getSeverity() == ValidationSeverity.HIGH
                        || validation.getSeverity() == ValidationSeverity.CRITICAL) {
                    abuseService.record(
                            parseUserId(request.user()),
                            AbuseType.DIFY_RESPONSE_MANIPULATION,
                            AbuseActionType.BLOCKED,
                            "Dify 응답 검증 실패: severity=" + validation.getSeverity()
                                    + ", rules=" + validation.getReason());
                    throw new CustomException(ErrorType.DIFY_RESPONSE_INVALID);
                }
            }

            return analysisResponse;

        } catch (CustomException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("[DifyClient] HTTP 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorType.DIFY_API_ERROR);
        } catch (Exception e) {
            log.error("[DifyClient] 예기치 않은 오류: {}", e.getMessage());
            throw new CustomException(ErrorType.DIFY_API_ERROR);
        }
    }

    public ServiceExecutionResponse runServiceExecution(ServiceExecutionRequest request) {
        String url = difyProperties.baseUrl() + WORKFLOW_RUN_PATH;
        log.debug("[DifyClient] 서비스 실행 요청 → {} | user={}", url, request.user());

        try {
            String rawBody = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + difyProperties.serviceExecutionApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(String.class);

            log.info("[DifyClient] 서비스 실행 응답 raw: {}", rawBody);

            // Dify 표준 응답 구조: {workflow_run_id, task_id, data: {status, outputs: {...}}}
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode dataNode = root.path("data");

            if (dataNode.isMissingNode() || !"succeeded".equals(dataNode.path("status").asText())) {
                String error = dataNode.path("error").asText("unknown");
                log.error("[DifyClient] 서비스 실행 워크플로우 실패: status={}, error={}",
                        dataNode.path("status").asText(), error);
                throw new CustomException(ErrorType.DIFY_API_ERROR);
            }

            // outputs 필드를 ServiceExecutionResponse.Data로 파싱
            JsonNode outputsNode = dataNode.path("outputs");
            ServiceExecutionResponse.Data outputs = objectMapper.treeToValue(
                    outputsNode, ServiceExecutionResponse.Data.class);

            ServiceExecutionResponse response = new ServiceExecutionResponse(200, "success", outputs);
            return response;

        } catch (CustomException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("[DifyClient] 서비스 실행 HTTP 호출 실패: {}", e.getMessage());
            throw new CustomException(ErrorType.DIFY_API_ERROR);
        } catch (Exception e) {
            log.error("[DifyClient] 서비스 실행 예기치 않은 오류: {}", e.getMessage());
            throw new CustomException(ErrorType.DIFY_API_ERROR);
        }
    }

    /**
     * request.user()(String)를 Long userId로 변환. 실패 시 null (기록은 유지).
     */
    private Long parseUserId(String user) {
        try {
            return Long.parseLong(user);
        } catch (Exception e) {
            return null;
        }
    }
}
