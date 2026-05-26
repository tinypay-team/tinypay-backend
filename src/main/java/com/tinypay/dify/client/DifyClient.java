package com.tinypay.dify.client;

import com.tinypay.dify.config.DifyProperties;
import com.tinypay.dify.dto.ChatAnalysisRequest;
import com.tinypay.dify.dto.ChatAnalysisResponse;
import com.tinypay.dify.dto.DifyWorkflowResponse;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Dify API HTTP 클라이언트
 *
 * base-url 예: http://15.164.179.132/v1
 * 호출 endpoint: /workflows/run
 * → 최종 URL: http://15.164.179.132/v1/workflows/run
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyClient {

    private static final String WORKFLOW_RUN_PATH = "/workflows/run";

    private final DifyProperties difyProperties;
    private final RestClient restClient;

    /**
     * 채팅 요청 분석 Workflow 호출 (blocking 모드)
     *
     * @param request Dify 요청 DTO
     * @return Dify 분석 결과
     * @throws CustomException DIFY_API_ERROR  - HTTP 오류 또는 서버 장애
     * @throws CustomException DIFY_RESPONSE_INVALID - 응답 파싱 불가 또는 code != 200
     */
    public ChatAnalysisResponse runChatAnalysis(ChatAnalysisRequest request) {
        String url = difyProperties.baseUrl() + WORKFLOW_RUN_PATH;
        log.debug("[DifyClient] 채팅 분석 요청 → {} | user={}", url, request.user());

        try {
            // Dify 실제 응답 구조: { workflow_run_id, task_id, data: { status, outputs, error } }
            DifyWorkflowResponse raw = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + difyProperties.chatAnalysisApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(DifyWorkflowResponse.class);

            // 워크플로우 실행 성공 여부 확인 (status = "succeeded")
            if (raw == null || !raw.isSucceeded()) {
                String errDetail = (raw != null && raw.data() != null) ? raw.data().error() : "null response";
                log.error("[DifyClient] 워크플로우 실패: {}", errDetail);
                throw new CustomException(ErrorType.DIFY_API_ERROR);
            }

            // outputs에서 비즈니스 응답 추출
            ChatAnalysisResponse.Data outputs = raw.getOutputs();
            if (outputs == null || outputs.responseType() == null) {
                log.error("[DifyClient] outputs 또는 response_type 없음");
                throw new CustomException(ErrorType.DIFY_RESPONSE_INVALID);
            }

            log.info("[DifyClient] 분석 완료: responseType={}", outputs.responseType());
            return new ChatAnalysisResponse(200, "success", outputs);

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
