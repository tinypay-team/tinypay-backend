package com.tinypay.security.validation;

import com.tinypay.dify.dto.ChatAnalysisResponse;

/**
 * Dify 응답 검증기
 *
 * Dify API 응답을 받은 직후 호출되어, 응답이 시스템 정책에 부합하는지 검증한다.
 * 검증 실패 시 ValidationResult.detected = true로 반환하며,
 * 호출자(DifyClient)는 결과를 보고 차단 또는 통과 결정한다.
 *
 * 검증 항목: ValidationRule enum 참고 (12개 룰)
 */
public interface DifyResponseValidator {

    /**
     * Dify 응답을 검증한다.
     *
     * @param response Dify에서 받은 응답
     * @return 검증 결과 (안전하면 isSafe() == true)
     */
    ValidationResult validate(ChatAnalysisResponse response);
}
