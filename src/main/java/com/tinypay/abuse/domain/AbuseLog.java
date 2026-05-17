package com.tinypay.abuse.domain;

import com.tinypay.finance.domain.Wallet;
import com.tinypay.global.common.entity.BaseTimeEntity;
import com.tinypay.request.domain.AiRequest;
import com.tinypay.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "abuse_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AbuseLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "ai_request_id")
    private AiRequest aiRequest;

    @Column(name = "abuse_type", nullable = false)
    private String abuseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_taken", nullable = false)
    private AbuseActionType actionTaken;

    @Lob
    private String detail;

    @Column(name = "ip_address")
    private String ipAddress;

    @Builder
    public AbuseLog(User user, Wallet wallet, AiRequest aiRequest, String abuseType, AbuseActionType actionTaken, String detail, String ipAddress
    ) {
        this.user = user;
        this.wallet = wallet;
        this.aiRequest = aiRequest;
        this.abuseType = abuseType;
        this.actionTaken = actionTaken == null ? AbuseActionType.LOGGED : actionTaken;
        this.detail = detail;
        this.ipAddress = ipAddress;
    }
}
