package com.tinypay.blockchain.exception;

/**
 * 영수증 검증 2단계 실패: 블록체인에서 실패한 트랜잭션 (status != 1)
 */
public class TransactionFailedException extends BlockchainException {

    public TransactionFailedException(String message) {
        super(message);
    }
}
