package com.tinypay.finance.controller;

import com.tinypay.finance.dto.request.UpdateMonthlyBudgetRequest;
import com.tinypay.finance.dto.request.UpdatePerPaymentLimitRequest;
import com.tinypay.finance.dto.response.UpdateMonthlyBudgetResponse;
import com.tinypay.finance.dto.response.UpdatePerPaymentLimitResponse;
import com.tinypay.finance.service.BudgetPolicyService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget-policy")
@RequiredArgsConstructor
public class BudgetPolicyController {

    private final BudgetPolicyService budgetPolicyService;

    @PatchMapping("/per-payment-limit")
    public ApiResponse<UpdatePerPaymentLimitResponse> updatePerPaymentLimit(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid UpdatePerPaymentLimitRequest request) {
        return ApiResponse.success(SuccessType.UPDATE_PER_PAYMENT_LIMIT_SUCCESS,
                budgetPolicyService.updatePerPaymentLimit(userId, request));
    }

    @PatchMapping("/monthly-budget")
    public ApiResponse<UpdateMonthlyBudgetResponse> updateMonthlyBudget(
            @RequestAttribute("userId") Long userId,
            @RequestBody @Valid UpdateMonthlyBudgetRequest request) {
        return ApiResponse.success(SuccessType.UPDATE_MONTHLY_BUDGET_SUCCESS,
                budgetPolicyService.updateMonthlyBudget(userId, request));
    }
}