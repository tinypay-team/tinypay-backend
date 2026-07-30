package com.tinypay.dify.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "ai_request_api_item",
        indexes = @Index(name = "idx_api_item_request_execution", columnList = "request_id, execution_order")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequestApiItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private AiRequest request;

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "provider_name")
    private String providerName;

    @Column(length = 255)
    private String description;

    @Column(name = "estimated_cost", nullable = false, precision = 18, scale = 6)
    private BigDecimal estimatedCost;

    @Column(name = "execution_order")
    private Integer executionOrder;

    @Column(name = "output_type", length = 50)
    private String outputType;

    @Builder
    public AiRequestApiItem(AiRequest request, String apiName, String providerName, String description, BigDecimal estimatedCost, Integer executionOrder, String outputType) {
        this.request = request;
        this.apiName = apiName;
        this.providerName = providerName;
        this.description = description;
        this.estimatedCost = estimatedCost;
        this.executionOrder = executionOrder;
        this.outputType = outputType;
    }
}
