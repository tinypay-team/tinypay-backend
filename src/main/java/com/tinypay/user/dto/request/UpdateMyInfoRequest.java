package com.tinypay.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMyInfoRequest {

    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
    private String nickname;

    @Pattern(regexp = "^https://[a-zA-Z0-9\\-]+\\.s3\\.[a-zA-Z0-9\\-]+\\.amazonaws\\.com/.+",
            message = "올바르지 않은 URL 형식입니다.")
    private String profileImage;
}