package com.tinypay.security.validation;

/**
 * Dify 응답 검증 룰
 *
 * 카테고리:
 * - 기본 형식 (1~3): 모든 응답 공통
 * - PAYMENT_REQUIRED (4~8): 결제 요청 검증 (핵심)
 * - 일관성 (9~10): 필드 간 모순 검증
 * - 보안 (11~12): 비정상 길이 등
 */
public enum ValidationRule {

    // ===== 기본 형식 검증 =====
    INVALID_RESPONSE_TYPE(
            ValidationSeverity.HIGH,
            "유효하지 않은 responseType"
    ),
    MISSING_RESPONSE_TYPE(
            ValidationSeverity.CRITICAL,
            "responseType 누락"
    ),
    INVALID_CURRENCY(
            ValidationSeverity.HIGH,
            "지원하지 않는 통화 (USDC만 허용)"
    ),

    // ===== PAYMENT_REQUIRED 검증 =====
    MISSING_REQUIRED_SERVICES(
            ValidationSeverity.CRITICAL,
            "PAYMENT_REQUIRED인데 required_services 누락"
    ),
    INVALID_COST(
            ValidationSeverity.CRITICAL,
            "비용이 0 이하 (조작 의심)"
    ),
    EXCESSIVE_COST(
            ValidationSeverity.HIGH,
            "비용 상한 초과"
    ),
    COST_MISMATCH(
            ValidationSeverity.CRITICAL,
            "total_estimated_cost와 항목 합계 불일치"
    ),
    INVALID_RISK_LEVEL(
            ValidationSeverity.HIGH,
            "유효하지 않은 risk_level"
    ),

    // ===== 일관성 검증 =====
    PAYMENT_FLAG_MISMATCH(
            ValidationSeverity.MEDIUM,
            "PAYMENT_REQUIRED인데 requires_paid_service != true"
    ),
    MISSING_UNSUPPORTED_REASON(
            ValidationSeverity.LOW,
            "UNSUPPORTED_REQUEST인데 unsupported_reason 누락"
    ),

    // ===== 보안 검증 =====
    EXCESSIVE_ANSWER_LENGTH(
            ValidationSeverity.MEDIUM,
            "answer 길이 비정상 (인젝션 의심)"
    ),
    EXCESSIVE_CONFIRMATION_LENGTH(
            ValidationSeverity.MEDIUM,
            "user_confirmation_message 길이 비정상"
    );

    private final ValidationSeverity severity;
    private final String description;

    ValidationRule(ValidationSeverity severity, String description) {
        this.severity = severity;
        this.description = description;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }
}
