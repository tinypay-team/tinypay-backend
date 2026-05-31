package com.tinypay.finance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentApproveResponse {

    private Long requestId;
    private String status;
    private PaymentInfo payment;
    private WalletInfo wallet;

    @Getter
    @Builder
    public static class PaymentInfo {
        private Long paymentId;
        private String orderId;
        private String transactionHash;
        private BigDecimal amount;
        private LocalDateTime executedAt;
    }

    @Getter
    @Builder
    public static class WalletInfo {
        private BigDecimal balance;
    }
}
