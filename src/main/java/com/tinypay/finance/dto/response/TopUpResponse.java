package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TopUpResponse {
    private Long walletId;
    private BigDecimal amount;
    private BigDecimal balance;
}