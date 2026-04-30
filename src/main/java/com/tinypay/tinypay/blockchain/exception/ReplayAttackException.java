package com.tinypay.tinypay.blockchain.exception;

/**
 * 영수증 검증 1단계 실패: 이미 사용된 트랜잭션 해시 (Replay Attack)
 */
public class ReplayAttackException extends BlockchainException {

    public ReplayAttackException(String message) {
        super(message);
    }
}
