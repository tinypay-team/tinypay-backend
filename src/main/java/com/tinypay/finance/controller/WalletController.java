package com.tinypay.finance.controller;

import com.tinypay.finance.dto.request.CreateWalletRequest;
import com.tinypay.finance.dto.request.TopUpRequest;
import com.tinypay.finance.dto.request.UpdateAutoPaymentRequest;
import com.tinypay.finance.dto.response.GetWalletResponse;
import com.tinypay.finance.dto.response.TopUpResponse;
import com.tinypay.finance.dto.response.UpdateAutoPaymentResponse;
import com.tinypay.finance.service.BudgetPolicyService;
import com.tinypay.finance.service.WalletService;
import com.tinypay.finance.service.WalletTopUpService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final BudgetPolicyService budgetPolicyService;
    private final WalletTopUpService walletTopUpService;

    @PostMapping
    public ApiResponse<?> createWallet(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateWalletRequest request) {
        walletService.createWallet(userId, request);
        return ApiResponse.success(SuccessType.CREATE_WALLET_SUCCESS);
    }

    @GetMapping
    public ApiResponse<GetWalletResponse> getWallet(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(SuccessType.GET_WALLET_SUCCESS, walletService.getWallet(userId));
    }

    @PostMapping("/{walletId}/top-up")
    public ApiResponse<TopUpResponse> topUp(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long walletId,
            @RequestBody @Valid TopUpRequest request) {
        return ApiResponse.success(SuccessType.TOP_UP_SUCCESS,
                walletTopUpService.topUp(userId, walletId, request));
    }

    @PatchMapping("/{walletId}/auto-payment")
    public ApiResponse<UpdateAutoPaymentResponse> updateAutoPayment(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long walletId,
            @RequestBody @Valid UpdateAutoPaymentRequest request) {
        return ApiResponse.success(SuccessType.UPDATE_AUTO_PAYMENT_SUCCESS,
                budgetPolicyService.updateAutoPayment(userId, walletId, request));
    }
}