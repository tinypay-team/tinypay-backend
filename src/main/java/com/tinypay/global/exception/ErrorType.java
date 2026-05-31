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
    MISSING_ID_TOKEN(HttpStatus.BAD_REQUEST, "id_token이 존재하지 않습니다."),

    MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "refresh_token이 존재하지 않습니다."),
    MISSING_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "access token이 존재하지 않습니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "token 형식이 올바르지 않습니다."),
    ESTIMATED_COST_MISMATCH(HttpStatus.BAD_REQUEST, "예상 결제 금액이 일치하지 않습니다."),
    PER_REQUEST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "건당 결제 한도를 초과했습니다."),
    MONTHLY_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "월 결제 한도를 초과했습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "지갑 잔액이 부족합니다."),
    WALLET_LOCKED(HttpStatus.BAD_REQUEST, "지갑이 잠겨있습니다."),

    /**
     * HTTP 401 (UNAUTHORIZED)
     */
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 ID Token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 access token입니다."),

    /**
     * HTTP 403 (FORBIDDEN)
     */
    REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 요청에 대한 접근 권한이 없습니다."),
    
    /**
     * HTTP 404 (NOT FOUND)
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅 세션입니다."),
    AI_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 AI 요청입니다."),
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑을 찾을 수 없습니다."),

    /**
     * HTTP 409 (CONFLICT)
     */
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_REQUEST_STATUS(HttpStatus.CONFLICT, "승인 가능한 상태의 요청이 아닙니다."),
    CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT, "취소 가능한 상태의 요청이 아닙니다."),

    /**
     * HTTP 500 (INTERNAL SERVER ERROR)
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다."),

    /**
     * HTTP 502 (BAD GATEWAY) - 외부 API 오류
     */
    DIFY_API_ERROR(HttpStatus.BAD_GATEWAY, "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    DIFY_RESPONSE_INVALID(HttpStatus.BAD_GATEWAY, "AI 서버로부터 유효하지 않은 응답을 받았습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
