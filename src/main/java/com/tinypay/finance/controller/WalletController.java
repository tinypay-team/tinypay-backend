package com.tinypay.finance.controller;

import com.tinypay.finance.dto.request.CreateWalletRequest;
import com.tinypay.finance.dto.response.GetWalletResponse;
import com.tinypay.finance.service.WalletService;
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
}