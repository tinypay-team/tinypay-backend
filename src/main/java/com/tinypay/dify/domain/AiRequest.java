package com.tinypay.dify.domain;

import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
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
@Table(name = "ai_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(name = "ai_server_request_id")
    private String aiServerRequestId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "prompt", columnDefinition = "LONGTEXT")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiRequestStatus status;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "estimated_total_cost", precision = 18, scale = 6)
    private BigDecimal estimatedTotalCost;

    @Column(name = "analysis_summary", columnDefinition = "LONGTEXT")
    private String analysisSummary;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "executed_services", columnDefinition = "LONGTEXT")
    private String executedServices;

    @Column(name = "generated_files", columnDefinition = "LONGTEXT")
    private String generatedFiles;

    @Builder
    public AiRequest(User user, ChatMessage message, ChatSession session, String aiServerRequestId, String serviceName, String prompt, AiRequestStatus status, String statusMessage, BigDecimal estimatedTotalCost, String analysisSummary
    ) {
        this.user = user;
        this.message = message;
        this.session = session;
        this.aiServerRequestId = aiServerRequestId;
        this.serviceName = serviceName;
        this.prompt = prompt;
        this.status = status == null ? AiRequestStatus.ANALYZING : status;
        this.statusMessage = statusMessage;
        this.estimatedTotalCost = estimatedTotalCost;
        this.analysisSummary = analysisSummary;
    }

    public void connectMessage(ChatMessage message) {
        this.message = message;
    }

    public void updateEstimatedResult(BigDecimal estimatedTotalCost, String analysisSummary) {
        this.estimatedTotalCost = estimatedTotalCost;
        this.analysisSummary = analysisSummary;
        this.status = AiRequestStatus.WAITING_APPROVAL;
    }

    public void approve() {
        this.status = AiRequestStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = AiRequestStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void startExecution() {
        this.status = AiRequestStatus.EXECUTING;
    }

    public void complete() {
        this.status = AiRequestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void completeWithResult(String executedServices, String generatedFiles) {
        this.status = AiRequestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.executedServices = executedServices;
        this.generatedFiles = generatedFiles;
    }

    public void fail(String statusMessage) {
        this.status = AiRequestStatus.FAILED;
        this.statusMessage = statusMessage;
    }
}
