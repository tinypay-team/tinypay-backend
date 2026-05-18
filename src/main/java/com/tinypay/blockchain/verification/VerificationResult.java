package com.tinypay.blockchain.verification;

/**
 * 영수증 검증 결과를 담는 불변 객체
 *
 * 사용 예:
 *   VerificationResult.success()                          → 검증 통과
 *   VerificationResult.fail(FailReason.REPLAY_ATTACK, "...") → 검증 실패
 */
public class VerificationResult {

    private final boolean valid;
    private final FailReason reason;
    private final String detail;

    /** 외부에서 직접 생성 못 하게 막음 (정적 팩토리 메서드만 사용) */
    private VerificationResult(boolean valid, FailReason reason, String detail) {
        this.valid = valid;
        this.reason = reason;
        this.detail = detail;
    }

    /** 검증 통과 결과 생성 */
    public static VerificationResult success() {
        return new VerificationResult(true, FailReason.SUCCESS, "검증 통과");
    }

    /** 검증 실패 결과 생성 */
    public static VerificationResult fail(FailReason reason, String detail) {
        return new VerificationResult(false, reason, detail);
    }

    public boolean isValid() {
        return valid;
    }

    public FailReason getReason() {
        return reason;
    }

    /** 실패한 단계 번호 (1~5), 성공 시 0 */
    public int getFailedStep() {
        return reason.getStep();
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return String.format("VerificationResult{valid=%s, reason=%s, step=%d, detail='%s'}",
                valid, reason, getFailedStep(), detail);
    }
}
