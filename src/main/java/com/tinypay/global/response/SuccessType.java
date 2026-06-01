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
    CHAT_MESSAGE_CREATE_SUCCESS(HttpStatus.CREATED, "메시지가 생성되었습니다."),
    GET_MY_INFO_SUCCESS(HttpStatus.OK, "내 정보 조회에 성공했습니다."),
    UPDATE_MY_INFO_SUCCESS(HttpStatus.OK, "내 정보 수정에 성공했습니다."),
    DELETE_USER_SUCCESS(HttpStatus.OK, "회원 탈퇴에 성공했습니다."),
    PAYMENT_SUCCESS(HttpStatus.CREATED, "결제에 성공했습니다."),
    UPDATE_PER_PAYMENT_LIMIT_SUCCESS(HttpStatus.OK, "1회 결제 한도 설정에 성공했습니다."),
    UPDATE_MONTHLY_BUDGET_SUCCESS(HttpStatus.OK, "이번 달 예산 설정에 성공했습니다."),
    GET_MY_PAGE_SUCCESS(HttpStatus.OK, "마이페이지 조회에 성공했습니다."),
    GET_PAYMENT_LIST_SUCCESS(HttpStatus.OK, "결제 내역 조회에 성공했습니다."),
    GET_CHAT_MESSAGE_LIST_SUCCESS(HttpStatus.OK, "채팅 메시지 조회에 성공했습니다."),
    GET_AI_REQUEST_STATUS_SUCCESS(HttpStatus.OK, "AI 요청 상태를 조회했습니다."),
    CANCEL_AI_REQUEST_SUCCESS(HttpStatus.OK, "AI 요청이 취소되었습니다."),
    GET_PAYMENT_DETAIL_SUCCESS(HttpStatus.OK, "결제 내역 세부 조회에 성공했습니다."),
    GET_WALLET_SUCCESS(HttpStatus.OK, "내 지갑 정보 조회에 성공했습니다."),
    SEND_VERIFICATION_CODE_SUCCESS(HttpStatus.OK, "인증번호가 발송되었습니다."),
    VERIFY_PHONE_NUMBER_SUCCESS(HttpStatus.OK, "전화번호 인증이 완료되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
