package com.tinypay.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class PaymentApproveRequest {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal estimatedCost;
}
