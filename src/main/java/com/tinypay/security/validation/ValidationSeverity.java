package com.tinypay.security.validation;

/**
 * Dify 응답 검증 실패 심각도
 *
 * - LOW: 정보성 (응답 형식 문제, 동작 가능)
 * - MEDIUM: 경고 (조작 의심)
 * - HIGH: 차단 (명백한 조작/오류)
 * - CRITICAL: 즉시 차단 + 어뷰징 기록 (결제 직결)
 */
public enum ValidationSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
