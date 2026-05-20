package com.tinypay.blockchain.service;

/**
 * 지갑 생성 결과 객체.
 *
 * BlockchainService.createWallet() 의 반환 타입으로,
 * 백엔드 팀이 Wallet 엔티티 저장 시 사용한다.
 *
 * - walletAddress: 0x... 형태의 EVM 지갑 주소 (공개해도 OK)
 * - encryptedPrivateKey: AES-256-GCM으로 암호화된 private key (DB에 저장)
 *
 * 평문 private key는 절대 외부로 노출되지 않으며,
 * 결제 시 KeyEncryptor.decrypt() 로 복호화하여 메모리 내에서만 사용한다.
 */
public class CreateWalletResult {

    private final String walletAddress;
    private final String encryptedPrivateKey;

    public CreateWalletResult(String walletAddress, String encryptedPrivateKey) {
        this.walletAddress = walletAddress;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }
}
