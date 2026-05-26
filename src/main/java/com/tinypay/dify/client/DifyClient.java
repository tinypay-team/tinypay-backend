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

//  Dify API HTTP 클라이언트
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyClient {

    private static final String WORKFLOW_RUN_PATH = "/workflows/run";

    private final DifyProperties difyProperties;
    private final RestClient restClient;

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
