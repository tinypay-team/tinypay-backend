package com.tinypay.security.validation;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

/**
 * Dify 응답 검증 결과
 *
 * 검증 통과 시: detected = false, severity = LOW, matchedRules 비어있음
 * 검증 실패 시: detected = true, severity = 최고 심각도, matchedRules 채워짐
 *
 * 호출자(DifyClient)는 isDetected() 보고 차단/통과 결정.
 */
@Getter
@Builder
public class ValidationResult {

    private final boolean detected;
    private final ValidationSeverity severity;

    @Singular
    private final List<ValidationRule> matchedRules;

    private final String reason;

    /**
     * 안전한 결과인가 (검증 통과)
     */
    public boolean isSafe() {
        return !detected;
    }
}
