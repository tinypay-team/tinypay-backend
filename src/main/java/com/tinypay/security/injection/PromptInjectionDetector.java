package com.tinypay.security.injection;

/**
 * 프롬프트 인젝션 검사기
 *
 * 사용자가 입력한 메시지에 프롬프트 인젝션 패턴이 포함되어 있는지 검사한다.
 * Dify에 메시지를 전달하기 전 1차 필터로 사용된다.
 */
public interface PromptInjectionDetector {

    /**
     * 메시지에 인젝션 패턴이 있는지 검사한다.
     *
     * @param message 검사할 사용자 입력 메시지
     * @return 검사 결과. 탐지 여부, 심각도, 매칭된 룰 목록 포함
     */
    DetectionResult detect(String message);
}
