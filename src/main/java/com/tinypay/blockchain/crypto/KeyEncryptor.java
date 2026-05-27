package com.tinypay.blockchain.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 기반 사용자 지갑 private key 암호화/복호화 유틸.
 *
 * - 알고리즘: AES/GCM/NoPadding (변조 감지 포함)
 * - 키 크기: 256-bit (32 bytes)
 * - IV 크기: 12 bytes (GCM 표준 권장)
 * - 인증 태그: 128-bit
 *
 * 저장 포맷: Base64( IV(12) || ciphertext || authTag(16) )
 *
 * 마스터 키는 application.yml의 blockchain.encryption.master-key 에서 주입.
 * (Base64 인코딩된 32바이트 키)
 */
@Component
public class KeyEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;        // GCM 권장 12 bytes
    private static final int TAG_LENGTH_BIT = 128;  // 인증 태그 128 bits

    private final String masterKeyBase64;
    private SecretKey masterKey;

    public KeyEncryptor(@Value("${blockchain.encryption.master-key}") String masterKeyBase64) {
        this.masterKeyBase64 = masterKeyBase64;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "마스터 키는 32바이트여야 합니다. 현재: " + keyBytes.length + "바이트. " +
                "openssl rand -base64 32 로 재생성하세요."
            );
        }
        this.masterKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 평문 private key를 암호화한다.
     *
     * @param plaintext 암호화할 private key (예: "0xabc..." 형태)
     * @return Base64 인코딩된 암호화 문자열 (IV + ciphertext + tag)
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, gcmSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // IV + ciphertext(+tag) 합쳐서 저장
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("private key 암호화 실패", e);
        }
    }

    /**
     * 암호화된 문자열을 복호화한다.
     *
     * @param ciphertextBase64 encrypt()가 반환한 Base64 문자열
     * @return 원본 평문 private key
     */
    public String decrypt(String ciphertextBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertextBase64);

            if (combined.length < IV_LENGTH) {
                throw new EncryptionException("암호문이 손상되었습니다 (IV 길이 부족)");
            }

            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, gcmSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            // GCM에서 변조 감지 시 AEADBadTagException 발생 → 여기로 옴
            throw new EncryptionException("private key 복호화 실패 (변조 가능성)", e);
        }
    }
}
