package com.tinypay.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentApproveRequest {

    @NotNull(message = "예상 금액이 존재하지 않습니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "예상 금액은 0보다 커야합니다.")
    private BigDecimal estimatedCost;
}
