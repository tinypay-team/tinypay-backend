package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAutoPaymentResponse {
    private boolean autoPaymentEnabled;
}