package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class GetPaymentDetailResponse {

    private List<ApiUsageItem> apiUsages;

    @Getter
    @Builder
    public static class ApiUsageItem {
        private String apiName;
        private BigDecimal cost;
    }
}