package com.tinypay.user.dto.request;

import lombok.Getter;

@Getter
public class UpdateMyInfoRequest {
    private String nickname;
    private String profileImage;
}