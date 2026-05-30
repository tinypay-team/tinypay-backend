package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetPaymentListResponse {

    private List<PaymentItem> payments;
    private Long nextCursor;

    @Getter
    @Builder
    public static class PaymentItem {
        private Long paymentId;
        private String serviceName;
        private BigDecimal paidAmount;
        private LocalDateTime executedAt;
        private String paymentStatus;
    }
}