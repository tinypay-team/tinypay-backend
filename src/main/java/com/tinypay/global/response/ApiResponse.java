package com.tinypay.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tinypay.global.exception.ErrorType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonPropertyOrder({"code", "message", "data"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 성공 응답 - 데이터 없음
     */
    public static ApiResponse<?> success(SuccessType successType) {
        return new ApiResponse<>(successType.getHttpStatusCode(), successType.getMessage());
    }

    /**
     * 성공 응답 - 데이터 포함
     */
    public static <T> ApiResponse<T> success(SuccessType successType, T data) {
        return new ApiResponse<>(successType.getHttpStatusCode(), successType.getMessage(), data);
    }

    /**
     * 에러 응답 - 기본 에러 메시지 사용
     */
    public static ApiResponse<?> error(ErrorType errorType) {
        return new ApiResponse<>(errorType.getHttpStatusCode(), errorType.getMessage());
    }

    /**
     * 에러 응답 - 커스텀 메시지 사용
     */
    public static ApiResponse<?> error(ErrorType errorType, String message) {
        return new ApiResponse<>(errorType.getHttpStatusCode(), message);
    }

    /**
     * 에러 응답 - 기본 메시지 + 데이터 포함
     */
    public static <T> ApiResponse<T> error(ErrorType errorType, T data) {
        return new ApiResponse<>(errorType.getHttpStatusCode(), errorType.getMessage(), data);
    }

    /**
     * 에러 응답 - 커스텀 메시지 + 데이터 포함
     */
    public static <T> ApiResponse<T> error(ErrorType errorType, String message, T data) {
        return new ApiResponse<>(errorType.getHttpStatusCode(), message, data);
    }

    /**
     * 개발 중 디버깅용 에러 응답
     */
    public static ApiResponse<Exception> errorDev(ErrorType errorType, Exception e) {
        return new ApiResponse<>(errorType.getHttpStatusCode(), errorType.getMessage(), e);
    }
}