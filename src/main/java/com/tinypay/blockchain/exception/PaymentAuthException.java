package com.tinypay.blockchain.exception;

/**
 * 결제 인증 실패 예외
 *
 * mintUsdc / 결제 호출 시 검증 단계에서 실패할 때 발생.
 * 실패 케이스:
 * - 본인인증(휴대폰) 미완료
 * - 결제 비밀번호 검증 실패
 * - Wallet 상태 비정상 (LOCKED / DISCONNECTED)
 */
public class PaymentAuthException extends BlockchainException {

    public PaymentAuthException(String message) {
        super(message);
    }
}
