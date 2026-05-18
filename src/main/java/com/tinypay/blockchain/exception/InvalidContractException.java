package com.tinypay.blockchain.exception;

/**
 * 영수증 검증 3단계 실패: 공식 MockUSDC 컨트랙트가 아닌 다른 컨트랙트로 송금됨
 */
public class InvalidContractException extends BlockchainException {

    public InvalidContractException(String message) {
        super(message);
    }
}
