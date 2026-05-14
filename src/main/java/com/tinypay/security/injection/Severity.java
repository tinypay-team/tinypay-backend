package com.tinypay.security.injection;

/**
 * 프롬프트 인젝션 탐지 심각도
 *
 * - LOW: 의심 패턴이나 위험도 낮음. 로그만 남기고 통과 가능
 * - MEDIUM: 명확한 인젝션 시도. 차단 권장
 * - HIGH: 시스템 조작 시도. 즉시 차단
 * - CRITICAL: 결제 금액/서비스 조작 등 금전적 피해 가능. 즉시 차단 + 어뷰징 카운트
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
