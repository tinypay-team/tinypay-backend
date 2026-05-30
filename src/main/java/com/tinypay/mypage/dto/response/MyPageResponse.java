package com.tinypay.mypage.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyPageResponse {

    private BigDecimal balance;
    private MonthlyBudget monthlyBudget;
    private BigDecimal perPaymentLimit;
    private List<RecentPayment> recentPayments;
    private MonthlyTransactionSummary monthlyTransactionSummary;

    @Getter
    @Builder
    public static class MonthlyBudget {
        private BigDecimal usedAmount;
        private BigDecimal limitAmount;
        private BigDecimal usageRate;
    }

    @Getter
    @Builder
    public static class RecentPayment {
        private String serviceName;
        private BigDecimal paidAmount;
        private LocalDateTime executedAt;
    }

    @Getter
    @Builder
    public static class MonthlyTransactionSummary {
        private long transactionCount;
        private BigDecimal averageTransactionAmount;
    }
}