package com.tinypay.global.exception;

import com.tinypay.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.tinypay.global.exception.ErrorType.INTERNAL_SERVER_ERROR;
import static com.tinypay.global.exception.ErrorType.REQUEST_VALIDATION_EXCEPTION;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 400 VALIDATION_ERROR -> 유효성 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {

        Errors errors = e.getBindingResult();
        Map<String, String> validateDetails = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validateDetails.put(validKeyName, error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(REQUEST_VALIDATION_EXCEPTION, validateDetails));
    }

    /**
     * CUSTOM_ERROR -> 사용자 정의 예외(CustomException) 처리, 에러 타입과 상태코드 커스텀해서 반환
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ApiResponse<?>> handleBusinessException(CustomException e) {

        log.error("[EXCEPTION] CustomException Occured: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus())
                   .body(ApiResponse.error(e.getErrorType(), e.getMessage()));
    }

    /**
     * 500 INTERNAL_SERVER -> 처리되지 않은 예외 전반 처리
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Exception>> handleException(final Exception e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(INTERNAL_SERVER_ERROR, e));
    }
}