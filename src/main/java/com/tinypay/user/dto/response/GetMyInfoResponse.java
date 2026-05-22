package com.tinypay.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMyInfoResponse {

    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long userId;
        private String email;
        private String nickname;
        private String profileImage;
    }
}