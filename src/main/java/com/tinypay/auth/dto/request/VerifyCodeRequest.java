package com.tinypay.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyCodeRequest {

    @NotBlank(message = "전화번호가 존재하지 않습니다.")
    @Pattern(regexp = "^010[0-9]{8}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    @NotBlank(message = "인증번호가 존재하지 않습니다.")
    private String verificationCode;
}