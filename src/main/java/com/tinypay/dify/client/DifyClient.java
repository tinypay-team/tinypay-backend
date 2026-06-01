package com.tinypay.dify.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinypay.dify.config.DifyProperties;
import com.tinypay.dify.dto.ChatAnalysisRequest;
import com.tinypay.dify.dto.ChatAnalysisResponse;
import com.tinypay.dify.dto.DifyWorkflowResponse;
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

                if (validation.getSeverity() == ValidationSeverity.HIGH
                        || validation.getSeverity() == ValidationSeverity.CRITICAL) {
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
}
