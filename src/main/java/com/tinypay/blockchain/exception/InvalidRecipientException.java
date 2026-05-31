package com.tinypay.blockchain.exception;

/**
 * 영수증 검증 4단계 실패: 수신자 주소가 예상과 다름
 */
public class InvalidRecipientException extends BlockchainException {

    public InvalidRecipientException(String message) {
        super(message);
    }
}
