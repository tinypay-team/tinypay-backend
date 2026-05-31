package com.tinypay.security.injection;

import java.util.regex.Pattern;

/**
 * 프롬프트 인젝션 검사 룰
 *
 * 각 룰은 정규식 패턴, 심각도, 사유를 가진다.
 * 한국어와 영어 패턴을 모두 포함하며,
 * TinyPay 결제 시스템 도메인에 특화하여 false positive를 최소화했다.
 */
public enum DetectionRule {

    // ===== 일반 LLM 인젝션 =====

    IGNORE_INSTRUCTIONS(
            "(이전\\s*지시(사항)?\\s*무시|위의?\\s*명령\\s*무시|ignore\\s+(previous|prior|above)\\s+instructions?)",
            Severity.HIGH,
            "시스템 지시 무시 시도"
    ),

    SYSTEM_PROMPT_LEAK(
            "(시스템\\s*프롬프트|너의?\\s*프롬프트\\s*(보여|알려)|system\\s+prompt|reveal\\s+your\\s+prompt)",
            Severity.HIGH,
            "시스템 프롬프트 추출 시도"
    ),

    JAILBREAK_DAN(
            "(DAN\\s*모드|do\\s+anything\\s+now|jailbreak|탈옥)",
            Severity.HIGH,
            "Jailbreak 시도 (DAN 등)"
    ),

    // ===== TinyPay 특화 =====

    RESPONSE_TYPE_MANIPULATION(
            "(response_type\\s*(을|를)?\\s*(ANSWER|UNSUPPORTED)|set\\s+response_type)",
            Severity.CRITICAL,
            "응답 분류(response_type) 조작 시도"
    ),

    COST_MANIPULATION(
            "((estimated_cost|unit_cost|total_estimated_cost)\\s*(을|를|:)?\\s*0|cost\\s*=\\s*0)",
            Severity.CRITICAL,
            "결제 금액 조작 시도"
    ),

    RISK_LEVEL_MANIPULATION(
            "(risk_level\\s*(을|를|:)?\\s*LOW|set\\s+risk_level)",
            Severity.HIGH,
            "위험도(risk_level) 조작 시도"
    ),

    SERVICE_INJECTION(
            "(service_name\\s*:\\s*['\"]|required_services\\s*:\\s*\\[|add\\s+service)",
            Severity.HIGH,
            "서비스 목록 위조 시도"
    );

    private final Pattern pattern;
    private final Severity severity;
    private final String reason;

    DetectionRule(String regex, Severity severity, String reason) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        this.severity = severity;
        this.reason = reason;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getReason() {
        return reason;
    }
}
