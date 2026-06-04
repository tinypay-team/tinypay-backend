package com.tinypay.finance.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import com.tinypay.dify.domain.AiRequest;
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
@Table(name = "payment_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private AiRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "tx_hash", nullable = false, unique = true)
    private String txHash;

    @Column(name = "payer_wallet_address", nullable = false)
    private String payerWalletAddress;

    @Column(name = "receiver_wallet_address", nullable = false)
    private String receiverWalletAddress;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "blockchain_network", nullable = false)
    private String blockchainNetwork;

    @Column(name = "auto_payment_used", nullable = false)
    private boolean autoPaymentUsed;

    @Builder
    public PaymentLog(User user, AiRequest request, Wallet wallet, String orderId, String txHash, String payerWalletAddress, String receiverWalletAddress, BigDecimal amount, PaymentStatus paymentStatus, VerificationStatus verificationStatus, LocalDateTime executedAt, LocalDateTime verifiedAt, String blockchainNetwork, Boolean autoPaymentUsed
    ) {
        this.user = user;
        this.request = request;
        this.wallet = wallet;
        this.orderId = orderId;
        this.txHash = txHash;
        this.payerWalletAddress = payerWalletAddress;
        this.receiverWalletAddress = receiverWalletAddress;
        this.amount = amount == null ? BigDecimal.ZERO.setScale(6) : amount;
        this.paymentStatus = paymentStatus == null ? PaymentStatus.PENDING : paymentStatus;
        this.verificationStatus = verificationStatus == null ? VerificationStatus.PENDING : verificationStatus;
        this.executedAt = executedAt == null ? LocalDateTime.now() : executedAt;
        this.verifiedAt = verifiedAt;
        this.blockchainNetwork = blockchainNetwork == null ? "POLYGON_AMOY" : blockchainNetwork;
        this.autoPaymentUsed = autoPaymentUsed != null && autoPaymentUsed;
    }

    public void markPaymentSuccess() {
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    public void markVerificationSuccess() {
        this.verificationStatus = VerificationStatus.SUCCESS;
        this.verifiedAt = LocalDateTime.now();
    }

    public void markVerificationFailed() {
        this.verificationStatus = VerificationStatus.FAILED;
        this.verifiedAt = LocalDateTime.now();
    }
}
