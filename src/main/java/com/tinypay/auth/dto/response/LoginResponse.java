package com.tinypay.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

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