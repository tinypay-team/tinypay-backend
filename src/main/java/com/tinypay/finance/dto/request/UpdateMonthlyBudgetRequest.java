package com.tinypay.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class UpdateMonthlyBudgetRequest {

    @NotNull(message = "monthlyBudget 값이 존재하지 않습니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "monthlyBudget 값은 0보다 커야 합니다.")
    private BigDecimal monthlyBudget;
}