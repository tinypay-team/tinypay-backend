package com.tinypay.abuse.domain;

/**
 * 어뷰징 종류
 *
 * AbuseLog 테이블에 기록될 어뷰징 케이스 분류.
 * AbuseActionType과 함께 사용된다 (왜 어뷰징 → 무슨 조치).
 */
public enum AbuseType {

    // ===== 결제/인증 관련 =====

    /** 결제 비밀번호 5회 실패 */
    PAYMENT_PASSWORD_BRUTE_FORCE,

    /** 본인인증 안 한 사용자가 결제 시도 */
    UNAUTHORIZED_PAYMENT_ATTEMPT,

    /** 비밀번호 미설정 사용자가 충전 시도 */
    PASSWORD_NOT_SET,

    /** 잠긴 지갑으로 결제/충전 시도 */
    LOCKED_WALLET_ACCESS,

    // ===== 블록체인 관련 =====

    /** 영수증 5단계 검증 실패 (재사용/위변조) */
    RECEIPT_VERIFICATION_FAILED,

    // ===== AI/프롬프트 관련 =====

    /** 프롬프트 인젝션 시도 */
    PROMPT_INJECTION,
    
    /** Dify 응답 검증 실패 (responseType/비용/필드 조작) */
    DIFY_RESPONSE_MANIPULATION,

    // ===== 일반 =====

    /** 짧은 시간 내 반복 요청 (Rate Limit 초과) */
    RATE_LIMIT_EXCEEDED
}
