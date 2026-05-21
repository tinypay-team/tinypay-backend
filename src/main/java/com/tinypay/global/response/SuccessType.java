package com.tinypay.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessType {

    /**
     * HTTP 200 OK
     */
    PROCESS_SUCCESS(HttpStatus.OK, "OK"),
    CREATE_CHAT_SESSION_SUCCESS(HttpStatus.CREATED, "새 채팅 세션이 생성되었습니다."),
    GET_CHAT_SESSION_LIST_SUCCESS(HttpStatus.OK, "채팅 세션 목록 조회에 성공했습니다."),
    CHAT_MESSAGE_CREATE_SUCCESS(HttpStatus.CREATED, "메시지가 생성되었습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
