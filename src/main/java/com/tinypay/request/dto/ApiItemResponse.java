package com.tinypay.request.dto;

import com.tinypay.dify.domain.AiRequestApiItem;

import java.math.BigDecimal;

public record ApiItemResponse(
        String apiName,
        String description,
        BigDecimal estimatedCost
) {
    public static ApiItemResponse from(AiRequestApiItem item) {
        return new ApiItemResponse(
                item.getApiName(),
                item.getDescription(),
                item.getEstimatedCost()
        );
    }
}
