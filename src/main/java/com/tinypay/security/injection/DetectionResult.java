package com.tinypay.security.injection;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 프롬프트 인젝션 검사 결과
 *
 * @param detected      인젝션 패턴 탐지 여부
 * @param severity      탐지된 위험 중 가장 높은 심각도 (탐지 안 됐으면 LOW)
 * @param matchedRules  걸린 룰 목록 (탐지 안 됐으면 빈 리스트)
 * @param reason        사람이 읽을 사유 (로그/디버깅용)
 */
@Getter
@Builder
public class DetectionResult {

    private final boolean detected;
    private final Severity severity;
    private final List<DetectionRule> matchedRules;
    private final String reason;

    /**
     * 탐지되지 않은 결과 (안전한 메시지)
     */
    public static DetectionResult safe() {
        return DetectionResult.builder()
                .detected(false)
                .severity(Severity.LOW)
                .matchedRules(Collections.emptyList())
                .reason("No injection pattern detected")
                .build();
    }
}
