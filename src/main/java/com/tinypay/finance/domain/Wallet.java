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
@Table(name = "wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "wallet_address", nullable = false, unique = true)
    private String walletAddress;

    @Lob
    @Column(name = "private_key_encrypted", nullable = false)
    private String privateKeyEncrypted;

    @Column(name = "blockchain_network", nullable = false)
    private String blockchainNetwork;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_status", nullable = false)
    private WalletStatus walletStatus;

    @Column(name = "wallet_password")
    private String walletPassword;

    @Column(name = "wallet_password_created_at")
    private LocalDateTime walletPasswordCreatedAt;

    @Builder
    public Wallet(User user, String walletAddress, String privateKeyEncrypted, String blockchainNetwork, BigDecimal balance, WalletStatus walletStatus
    ) {
        this.user = user;
        this.walletAddress = walletAddress;
        this.privateKeyEncrypted = privateKeyEncrypted;
        this.blockchainNetwork = blockchainNetwork == null ? "POLYGON_AMOY" : blockchainNetwork;
        this.balance = balance == null ? BigDecimal.ZERO.setScale(6) : balance;
        this.walletStatus = walletStatus == null ? WalletStatus.ACTIVE : walletStatus;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void disconnect() {
        this.walletStatus = WalletStatus.DISCONNECTED;
    }

    public void lock() {
        this.walletStatus = WalletStatus.LOCKED;
    }
}
