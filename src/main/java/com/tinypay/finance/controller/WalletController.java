package com.tinypay.finance.controller;

import com.tinypay.finance.dto.response.GetWalletResponse;
import com.tinypay.finance.service.WalletService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ApiResponse<GetWalletResponse> getWallet(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(SuccessType.GET_WALLET_SUCCESS, walletService.getWallet(userId));
    }
}