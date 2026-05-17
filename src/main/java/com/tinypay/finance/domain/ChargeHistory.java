package com.tinypay.finance.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import com.tinypay.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "charge_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargeHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal amount;

    @Column(name = "balance_snapshot", nullable = false, precision = 18, scale = 6)
    private BigDecimal balanceSnapshot;

    @Column(name = "tx_hash")
    private String txHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_status", nullable = false)
    private ChargeStatus chargeStatus;

    @Builder
    public ChargeHistory(User user, Wallet wallet, BigDecimal amount, BigDecimal balanceSnapshot, String txHash, ChargeStatus chargeStatus
    ) {
        this.user = user;
        this.wallet = wallet;
        this.amount = amount == null ? BigDecimal.ZERO.setScale(6) : amount;
        this.balanceSnapshot = balanceSnapshot == null ? BigDecimal.ZERO.setScale(6) : balanceSnapshot;
        this.txHash = txHash;
        this.chargeStatus = chargeStatus == null ? ChargeStatus.PENDING : chargeStatus;
    }

    public void markSuccess(BigDecimal balanceSnapshot, String txHash) {
        this.chargeStatus = ChargeStatus.SUCCESS;
        this.balanceSnapshot = balanceSnapshot;
        this.txHash = txHash;
    }

    public void markFailed() {
        this.chargeStatus = ChargeStatus.FAILED;
    }
}
