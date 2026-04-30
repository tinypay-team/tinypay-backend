package com.tinypay.tinypay.blockchain.exception;

/**
 * 블록체인 관련 모든 예외의 부모 클래스
 * 백엔드 팀이 catch (BlockchainException e) 한 줄로 모든 블록체인 예외를 잡을 수 있도록 설계
 */
public class BlockchainException extends RuntimeException {

    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
