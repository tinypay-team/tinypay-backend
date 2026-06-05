package com.tinypay.security.validation;

import com.tinypay.dify.dto.ChatAnalysisResponse;
import com.tinypay.dify.dto.RequiredServiceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DifyResponseValidatorImpl 단위 테스트 (핵심 10개)
 *
 * 카테고리:
 * - 정상 통과: 3개 (ANSWER / PAYMENT / UNSUPPORTED)
 * - 기본 형식: 1개
 * - PAYMENT_REQUIRED 검증: 4개
 * - 보안: 1개
 * - 종합: 1개
 */
class DifyResponseValidatorImplTest {

    private final DifyResponseValidatorImpl validator = new DifyResponseValidatorImpl();

    // ===== 정상 통과 =====
    @Nested
    @DisplayName("정상 응답은 통과")
    class ValidResponses {

        @Test
        @DisplayName("ANSWER 정상 응답 → 검증 통과")
        void answerResponse_passes() {
            ChatAnalysisResponse response = answer("Paris is the capital of France.");
            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isTrue();
            assertThat(result.getMatchedRules()).isEmpty();
        }

        @Test
        @DisplayName("PAYMENT_REQUIRED 정상 응답 → 검증 통과")
        void paymentRequired_passes() {
            ChatAnalysisResponse response = paymentRequired(
                    new BigDecimal("0.5"),
                    "LOW",
                    "USDC",
                    List.of(service("video_gen", new BigDecimal("0.5")))
            );
            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isTrue();
        }

        @Test
        @DisplayName("UNSUPPORTED_REQUEST 정상 응답 → 검증 통과")
        void unsupportedRequest_passes() {
            ChatAnalysisResponse response = unsupported("기능 미지원");
            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isTrue();
        }
    }

    // ===== 기본 형식 검증 =====
    @Nested
    @DisplayName("기본 형식 검증")
    class BasicFormat {

        @Test
        @DisplayName("invalid responseType → INVALID_RESPONSE_TYPE 검출")
        void invalidResponseType_detected() {
            ChatAnalysisResponse.Data data = new ChatAnalysisResponse.Data(
                    null, null, "INVALID_TYPE", null,
                    "test", null, null, null,
                    null, null, null, null, null, null, null, null
            );
            ChatAnalysisResponse response = new ChatAnalysisResponse(200, "success", data);

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getMatchedRules()).contains(ValidationRule.INVALID_RESPONSE_TYPE);
            assertThat(result.getSeverity()).isEqualTo(ValidationSeverity.HIGH);
        }
    }

    // ===== PAYMENT_REQUIRED 검증 =====
    @Nested
    @DisplayName("PAYMENT_REQUIRED 검증")
    class PaymentValidation {

        @Test
        @DisplayName("required_services 비어있음 → MISSING_REQUIRED_SERVICES")
        void emptyServices_detected() {
            ChatAnalysisResponse response = paymentRequired(
                    new BigDecimal("1.0"), "LOW", "USDC", List.of()
            );

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getMatchedRules()).contains(ValidationRule.MISSING_REQUIRED_SERVICES);
            assertThat(result.getSeverity()).isEqualTo(ValidationSeverity.CRITICAL);
        }

        @Test
        @DisplayName("cost가 0 → INVALID_COST")
        void zeroCost_detected() {
            ChatAnalysisResponse response = paymentRequired(
                    BigDecimal.ZERO, "LOW", "USDC",
                    List.of(service("video_gen", BigDecimal.ZERO))
            );

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getMatchedRules()).contains(ValidationRule.INVALID_COST);
        }

        @Test
        @DisplayName("총합 불일치 → COST_MISMATCH")
        void costMismatch_detected() {
            ChatAnalysisResponse response = paymentRequired(
                    new BigDecimal("10.0"),  // 총합 10
                    "LOW",
                    "USDC",
                    List.of(
                            service("api_a", new BigDecimal("1.0")),
                            service("api_b", new BigDecimal("2.0"))
                            // 실제 합: 3.0인데 총합은 10 → 불일치
                    )
            );

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getMatchedRules()).contains(ValidationRule.COST_MISMATCH);
        }

        @Test
        @DisplayName("invalid risk_level → INVALID_RISK_LEVEL")
        void invalidRiskLevel_detected() {
            ChatAnalysisResponse response = paymentRequired(
                    new BigDecimal("1.0"),
                    "EXTREME",  // 유효하지 않음
                    "USDC",
                    List.of(service("video_gen", new BigDecimal("1.0")))
            );

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getMatchedRules()).contains(ValidationRule.INVALID_RISK_LEVEL);
        }
    }

    // ===== 보안 검증 =====
    @Nested
    @DisplayName("보안 검증")
    class Security {

        @Test
        @DisplayName("answer 5000자 초과 → EXCESSIVE_ANSWER_LENGTH")
        void excessiveAnswer_detected() {
            String longAnswer = "A".repeat(5001);
            ChatAnalysisResponse response = answer(longAnswer);

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getMatchedRules()).contains(ValidationRule.EXCESSIVE_ANSWER_LENGTH);
        }
    }

    // ===== 종합 =====
    @Nested
    @DisplayName("종합 검증")
    class Integration {

        @Test
        @DisplayName("여러 룰 동시 실패 → 최고 심각도 + 모든 매칭 룰 포함")
        void multipleFailures_aggregated() {
            // CRITICAL(MISSING_REQUIRED_SERVICES) + MEDIUM(PAYMENT_FLAG_MISMATCH) 동시 발생
            ChatAnalysisResponse.Data data = new ChatAnalysisResponse.Data(
                    null, null, "PAYMENT_REQUIRED", null,
                    null,
                    "false",  // requiresPaidService - 모순
                    null,
                    List.of(),  // 빈 services
                    null, "USDC", null, null, null, null, null, null
            );
            ChatAnalysisResponse response = new ChatAnalysisResponse(200, "success", data);

            ValidationResult result = validator.validate(response);

            assertThat(result.isSafe()).isFalse();
            assertThat(result.getSeverity()).isEqualTo(ValidationSeverity.CRITICAL);
            assertThat(result.getMatchedRules())
                    .contains(ValidationRule.MISSING_REQUIRED_SERVICES)
                    .contains(ValidationRule.PAYMENT_FLAG_MISMATCH);
        }
    }

    // ===== 헬퍼 메서드 =====

    private ChatAnalysisResponse answer(String answerText) {
        ChatAnalysisResponse.Data data = new ChatAnalysisResponse.Data(
                null, null, "ANSWER", null,
                answerText, "false", "false",
                null, null, null, null, null, null, null, null, null
        );
        return new ChatAnalysisResponse(200, "success", data);
    }

    private ChatAnalysisResponse paymentRequired(
            BigDecimal totalCost,
            String riskLevel,
            String currency,
            List<RequiredServiceDto> services
    ) {
        ChatAnalysisResponse.Data data = new ChatAnalysisResponse.Data(
                null, null, "PAYMENT_REQUIRED", null,
                null, "true", "true",
                services, totalCost, currency, riskLevel,
                "결제 진행하시겠습니까?",
                null, null, null, null
        );
        return new ChatAnalysisResponse(200, "success", data);
    }

    private ChatAnalysisResponse unsupported(String reason) {
        ChatAnalysisResponse.Data data = new ChatAnalysisResponse.Data(
                null, null, "UNSUPPORTED_REQUEST", null,
                null, "false", "false",
                null, null, null, null, null,
                reason, List.of(), List.of(), null
        );
        return new ChatAnalysisResponse(200, "success", data);
    }

    private RequiredServiceDto service(String name, BigDecimal cost) {
        return new RequiredServiceDto(
                name, "API", "test purpose", cost,
                "USDC", "LOW", "TEXT", "test reason"
        );
    }
}
