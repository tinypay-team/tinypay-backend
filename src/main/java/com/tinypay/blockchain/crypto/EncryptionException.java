package com.tinypay.blockchain.crypto;

import com.tinypay.blockchain.exception.BlockchainException;

/**
 * private key 암호화/복호화 실패 시 던지는 예외.
 * GCM 변조 감지, 손상된 암호문, 잘못된 마스터 키 등이 원인.
 */
public class EncryptionException extends BlockchainException {

    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
