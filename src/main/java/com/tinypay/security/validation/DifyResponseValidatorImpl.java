package com.tinypay.security.validation;

import com.tinypay.dify.dto.ChatAnalysisResponse;
import com.tinypay.dify.dto.RequiredServiceDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dify 응답 검증기 구현체
 *
 * 12개 룰 (ValidationRule 참고)을 순차 검사하여 결과를 ValidationResult로 반환한다.
 * 인젝션 모듈과 달리 fail-fast가 아니라 모든 룰을 검사 후 결과 모음.
 * (이유: AbuseLog에 풍부한 정보 제공 + 디버깅 용이)
 */
@Component
public class DifyResponseValidatorImpl implements DifyResponseValidator {

    // ===== 유효 값 정의 =====
    private static final Set<String> VALID_RESPONSE_TYPES = Set.of(
            "ANSWER", "PAYMENT_REQUIRED", "UNSUPPORTED_REQUEST"
    );

    private static final Set<String> VALID_RISK_LEVELS = Set.of(
            "LOW", "MEDIUM", "HIGH"
    );

    private static final Set<String> VALID_CURRENCIES = Set.of(
            "USDC"
    );

    // ===== 정책 상수 =====
    private static final BigDecimal MAX_COST = new BigDecimal("100");
    private static final BigDecimal COST_TOLERANCE = new BigDecimal("0.01");
    private static final int MAX_ANSWER_LENGTH = 5000;
    private static final int MAX_CONFIRMATION_LENGTH = 1000;

    @Override
    public ValidationResult validate(ChatAnalysisResponse response) {
        List<ValidationRule> matched = new ArrayList<>();

        // 1. 응답 자체 null 체크
        if (response == null || response.data() == null) {
            return buildFailResult(
                    List.of(ValidationRule.MISSING_RESPONSE_TYPE),
                    "응답 또는 data 누락"
            );
        }

        ChatAnalysisResponse.Data data = response.data();
        String responseType = data.responseType();

        // ===== 카테고리 1: 기본 형식 검증 =====
        if (responseType == null || responseType.isBlank()) {
            matched.add(ValidationRule.MISSING_RESPONSE_TYPE);
        } else if (!VALID_RESPONSE_TYPES.contains(responseType)) {
            matched.add(ValidationRule.INVALID_RESPONSE_TYPE);
        }

        if (data.currency() != null && !VALID_CURRENCIES.contains(data.currency())) {
            matched.add(ValidationRule.INVALID_CURRENCY);
        }

        // ===== 카테고리 2: PAYMENT_REQUIRED 검증 =====
        if ("PAYMENT_REQUIRED".equals(responseType)) {
            validatePaymentRequired(data, matched);
        }

        // ===== 카테고리 3: 일관성 검증 =====
        validateConsistency(data, responseType, matched);

        // ===== 카테고리 4: 보안 검증 =====
        validateSecurity(data, matched);

        // ===== 결과 빌드 =====
        if (matched.isEmpty()) {
            return ValidationResult.builder()
                    .detected(false)
                    .severity(ValidationSeverity.LOW)
                    .reason("검증 통과")
                    .build();
        }

        return buildFailResult(matched, buildReason(matched));
    }

    /**
     * PAYMENT_REQUIRED 케이스 검증 (룰 4~8)
     */
    private void validatePaymentRequired(ChatAnalysisResponse.Data data,
                                          List<ValidationRule> matched) {
        List<RequiredServiceDto> services = data.requiredServices();

        // 룰 4: required_services 누락
        if (services == null || services.isEmpty()) {
            matched.add(ValidationRule.MISSING_REQUIRED_SERVICES);
            return;  // 이후 비용 검증 불가능
        }

        BigDecimal totalCost = data.totalEstimatedCost();

        // 룰 5: 비용 0 이하
        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) <= 0) {
            matched.add(ValidationRule.INVALID_COST);
            return;  // 이후 검증 의미 없음
        }

        // 룰 6: 비용 상한 초과
        if (totalCost.compareTo(MAX_COST) > 0) {
            matched.add(ValidationRule.EXCESSIVE_COST);
        }

        // 룰 7: 총합 일치 검증
        BigDecimal sumOfServices = services.stream()
                .map(RequiredServiceDto::estimatedCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal diff = totalCost.subtract(sumOfServices).abs();
        if (diff.compareTo(COST_TOLERANCE) > 0) {
            matched.add(ValidationRule.COST_MISMATCH);
        }

        // 룰 8: risk_level 검증
        String riskLevel = data.riskLevel();
        if (riskLevel != null && !VALID_RISK_LEVELS.contains(riskLevel)) {
            matched.add(ValidationRule.INVALID_RISK_LEVEL);
        }
    }

    /**
     * 일관성 검증 (룰 9~10)
     */
    private void validateConsistency(ChatAnalysisResponse.Data data,
                                      String responseType,
                                      List<ValidationRule> matched) {
        // 룰 9: PAYMENT_REQUIRED인데 requiresPaidService != "true"
        if ("PAYMENT_REQUIRED".equals(responseType)) {
            String paidFlag = data.requiresPaidService();
            if (paidFlag == null || !"true".equalsIgnoreCase(paidFlag)) {
                matched.add(ValidationRule.PAYMENT_FLAG_MISMATCH);
            }
        }

        // 룰 10: UNSUPPORTED_REQUEST인데 unsupported_reason 누락
        if ("UNSUPPORTED_REQUEST".equals(responseType)) {
            String reason = data.unsupportedReason();
            if (reason == null || reason.isBlank()) {
                matched.add(ValidationRule.MISSING_UNSUPPORTED_REASON);
            }
        }
    }

    /**
     * 보안 검증 (룰 11~12)
     */
    private void validateSecurity(ChatAnalysisResponse.Data data,
                                   List<ValidationRule> matched) {
        // 룰 11: answer 길이
        if (data.answer() != null && data.answer().length() > MAX_ANSWER_LENGTH) {
            matched.add(ValidationRule.EXCESSIVE_ANSWER_LENGTH);
        }

        // 룰 12: userConfirmationMessage 길이
        if (data.userConfirmationMessage() != null
                && data.userConfirmationMessage().length() > MAX_CONFIRMATION_LENGTH) {
            matched.add(ValidationRule.EXCESSIVE_CONFIRMATION_LENGTH);
        }
    }

    /**
     * 검증 실패 결과 빌드 - 최고 심각도 추출
     */
    private ValidationResult buildFailResult(List<ValidationRule> matched, String reason) {
        ValidationSeverity highestSeverity = matched.stream()
                .map(ValidationRule::getSeverity)
                .max(Enum::compareTo)
                .orElse(ValidationSeverity.LOW);

        ValidationResult.ValidationResultBuilder builder = ValidationResult.builder()
                .detected(true)
                .severity(highestSeverity)
                .reason(reason);

        for (ValidationRule rule : matched) {
            builder.matchedRule(rule);
        }

        return builder.build();
    }

    /**
     * 사유 메시지 빌드 (룰 이름들 콤마 연결)
     */
    private String buildReason(List<ValidationRule> matched) {
        StringBuilder sb = new StringBuilder("검증 실패: ");
        for (int i = 0; i < matched.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(matched.get(i).name());
        }
        return sb.toString();
    }
}
