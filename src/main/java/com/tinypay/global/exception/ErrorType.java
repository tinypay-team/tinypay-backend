package com.tinypay.global.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorType {

    /**
     * HTTP 400 (BAD REQUEST)
     */
    REQUEST_VALIDATION_EXCEPTION(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    /**
     * HTTP 401 (UNAUTHORIZED)
     */


    /**
     * HTTP 404 (NOT FOUND)
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅 세션입니다."),
    AI_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 AI 요청입니다."),

    /**
     * HTTP 502 (BAD GATEWAY) - 외부 API 오류
     */
    DIFY_API_ERROR(HttpStatus.BAD_GATEWAY, "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    DIFY_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "AI 서버로부터 유효하지 않은 응답을 받았습니다."),

    /**
     * HTTP 500 (INTERNAL SERVER ERROR)
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
