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
    INVALID_NICKNAME_LENGTH(HttpStatus.BAD_REQUEST, "닉네임은 2자 이상 10자 이하여야 합니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다."),
    INVALID_URL_FORMAT(HttpStatus.BAD_REQUEST, "올바르지 않은 URL 형식입니다."),

    /**
     * HTTP 401 (UNAUTHORIZED)
     */
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 ID Token입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 access token입니다."),

    /**
     * HTTP 404 (NOT FOUND)
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    /**
     * HTTP 409 (CONFLICT)
     */
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

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
