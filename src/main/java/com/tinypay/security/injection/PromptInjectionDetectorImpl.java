package com.tinypay.security.injection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 프롬프트 인젝션 검사기 구현체
 *
 * 정규식 기반 패턴 매칭으로 1차 필터링을 수행한다.
 * 모든 룰을 검사하여 다중 매칭을 지원하며, 가장 높은 심각도를 결과 severity로 사용한다.
 */
@Slf4j
@Component
public class PromptInjectionDetectorImpl implements PromptInjectionDetector {

    /** 메시지 최대 길이. 초과 시 잘라서 검사 (ReDoS 방지) */
    private static final int MAX_MESSAGE_LENGTH = 10_000;

    @Override
    public DetectionResult detect(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        if (message.isBlank()) {
            return DetectionResult.safe();
        }

        String target = truncateIfTooLong(message);

        List<DetectionRule> matched = new ArrayList<>();
        for (DetectionRule rule : DetectionRule.values()) {
            if (rule.getPattern().matcher(target).find()) {
                matched.add(rule);
            }
        }

        if (matched.isEmpty()) {
            return DetectionResult.safe();
        }

        Severity highestSeverity = matched.stream()
                .map(DetectionRule::getSeverity)
                .max(Comparator.comparingInt(Enum::ordinal))
                .orElse(Severity.LOW);

        String reason = matched.stream()
                .map(DetectionRule::getReason)
                .collect(Collectors.joining(", "));

        log.warn("[PromptInjection] detected={}, severity={}, rules={}",
                true, highestSeverity, matched);

        return DetectionResult.builder()
                .detected(true)
                .severity(highestSeverity)
                .matchedRules(matched)
                .reason(reason)
                .build();
    }

    private String truncateIfTooLong(String message) {
        if (message.length() <= MAX_MESSAGE_LENGTH) {
            return message;
        }
        log.warn("[PromptInjection] message truncated: original={}, truncated={}",
                message.length(), MAX_MESSAGE_LENGTH);
        return message.substring(0, MAX_MESSAGE_LENGTH);
    }
}
