package com.tinypay.finance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAutoPaymentRequest {

    @NotNull(message = "enabled 값이 존재하지 않습니다.")
    private Boolean enabled;

    @Pattern(regexp = "^[0-9]{6}$", message = "지갑 비밀번호는 6자리 숫자여야 합니다.")
    private String walletPassword;
}