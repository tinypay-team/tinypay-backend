package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GetWalletResponse {

    private String walletAddress;
    private BigDecimal balance;
    private String walletStatus;
    private boolean autoPaymentEnabled;
}