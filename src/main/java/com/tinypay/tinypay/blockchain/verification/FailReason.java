package com.tinypay.tinypay.blockchain.verification;

/**
 * 영수증 검증 실패 사유
 * 검증 5단계 각각의 실패 케이스 + 성공 케이스
 */
public enum FailReason {

    /** 검증 통과 */
    SUCCESS(0, "검증 통과"),

    /** 1단계 실패: 이미 사용된 트랜잭션 해시 */
    REPLAY_ATTACK(1, "이미 사용된 영수증 (Replay Attack)"),

    /** 2단계 실패: 블록체인에서 실패한 트랜잭션 */
    TRANSACTION_FAILED(2, "트랜잭션 실패 (status != 1)"),

    /** 3단계 실패: 공식 MockUSDC 컨트랙트가 아님 */
    INVALID_CONTRACT(3, "공식 USDC 컨트랙트가 아님"),

    /** 4단계 실패: 수신자 주소 불일치 */
    INVALID_RECIPIENT(4, "수신자 주소 불일치"),

    /** 5단계 실패: 결제 금액 부족 */
    INSUFFICIENT_PAYMENT(5, "결제 금액 부족");

    private final int step;
    private final String description;

    FailReason(int step, String description) {
        this.step = step;
        this.description = description;
    }

    public int getStep() {
        return step;
    }

    public String getDescription() {
        return description;
    }
}
