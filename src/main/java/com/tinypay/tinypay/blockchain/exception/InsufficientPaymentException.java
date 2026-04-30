package com.tinypay.tinypay.blockchain.exception;

/**
 * 영수증 검증 5단계 실패: 결제 금액이 예상보다 부족함
 */
public class InsufficientPaymentException extends BlockchainException {

    public InsufficientPaymentException(String message) {
        super(message);
    }
}
