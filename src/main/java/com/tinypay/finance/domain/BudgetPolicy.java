package com.tinypay.finance.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import com.tinypay.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "budget_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "per_request_limit", precision = 18, scale = 6)
    private BigDecimal perRequestLimit;

    @Column(name = "monthly_limit", precision = 18, scale = 6)
    private BigDecimal monthlyLimit;

    @Column(name = "auto_payment_enabled", nullable = false)
    private boolean autoPaymentEnabled;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public BudgetPolicy(User user, BigDecimal perRequestLimit, BigDecimal monthlyLimit, Boolean autoPaymentEnabled
    ) {
        this.user = user;
        this.perRequestLimit = perRequestLimit;
        this.monthlyLimit = monthlyLimit;
        this.autoPaymentEnabled = autoPaymentEnabled != null && autoPaymentEnabled;
    }

    public void updateLimits(BigDecimal perRequestLimit, BigDecimal monthlyLimit) {
        this.perRequestLimit = perRequestLimit;
        this.monthlyLimit = monthlyLimit;
    }

    public void updatePerRequestLimit(BigDecimal perRequestLimit) {
        this.perRequestLimit = perRequestLimit;
    }

    public void updateMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public void updateAutoPaymentEnabled(boolean autoPaymentEnabled) {
        this.autoPaymentEnabled = autoPaymentEnabled;
    }
}
