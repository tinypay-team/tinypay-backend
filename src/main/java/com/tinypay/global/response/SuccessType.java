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
    LOGIN_SUCCESS(HttpStatus.OK, "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃에 성공했습니다."),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "토큰 재발급에 성공했습니다."),
    CREATE_CHAT_SESSION_SUCCESS(HttpStatus.CREATED, "새 채팅 세션이 생성되었습니다."),
    GET_CHAT_SESSION_LIST_SUCCESS(HttpStatus.OK, "채팅 세션 목록 조회에 성공했습니다."),
    GET_MY_INFO_SUCCESS(HttpStatus.OK, "내 정보 조회에 성공했습니다."),
    UPDATE_MY_INFO_SUCCESS(HttpStatus.OK, "내 정보 수정에 성공했습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
