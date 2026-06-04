package com.tinypay.abuse.service;

import com.tinypay.abuse.domain.AbuseActionType;
import com.tinypay.abuse.domain.AbuseType;

/**
 * 어뷰징 기록 서비스
 *
 * 시스템 곳곳에서 발생하는 어뷰징을 AbuseLog로 일관되게 기록한다.
 * Rate Limit 위반 감지는 백엔드 영역이며, 감지 후 이 서비스의 메서드를 호출한다.
 */
public interface AbuseService {

    /**
     * 범용 어뷰징 기록.
     *
     * @param userId 대상 사용자 ID (null 허용 — 조회 실패 시에도 기록)
     * @param type   어뷰징 종류
     * @param action 취해진 조치
     * @param detail 상세 사유 (로그/분석용)
     */
    void record(Long userId, AbuseType type, AbuseActionType action, String detail);

    /**
     * Rate Limit 위반 기록 (편의 메서드).
     * 백엔드가 Redis 카운트로 위반 감지 후 호출한다.
     *
     * @param userId 위반 사용자 ID
     * @param detail 상세 사유 (예: "결제 API 1분당 한도 초과")
     */
    void recordRateLimitViolation(Long userId, String detail);
}
