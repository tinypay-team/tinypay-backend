package com.tinypay.blockchain.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제/충전 시 검증 컨텍스트
 *
 * 서버(백엔드)가 사용자 인증/검증 결과를 모아서 블록체인 모듈에 전달한다.
 * 블록체인 모듈은 이 컨텍스트를 받아 자체 검증을 한 번 더 수행한다 (이중 방어).
 *
 * @param userId             요청 사용자 ID (DB 재조회용)
 * @param isPasswordVerified 결제 비밀번호 검증 통과 여부
 * @param isPhoneVerified    휴대폰 본인인증 완료 여부
 * @param walletStatus       지갑 상태 (ACTIVE / LOCKED / DISCONNECTED)
 */
@Getter
@Builder
public class PaymentAuthContext {

    private final Long userId;
    private final boolean isPasswordVerified;
    private final boolean isPhoneVerified;
    private final String walletStatus;
}
