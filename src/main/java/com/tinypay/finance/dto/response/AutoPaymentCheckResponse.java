package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoPaymentCheckResponse {

    private boolean autoPaymentEnabled;
    private boolean exceedsPerPaymentLimit;
}