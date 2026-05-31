package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class UpdatePerPaymentLimitResponse {

    private BigDecimal perPaymentLimit;
}