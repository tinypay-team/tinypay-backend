package com.tinypay.finance.service;

import com.tinypay.finance.domain.BudgetPolicy;
import com.tinypay.finance.domain.Wallet;
import com.tinypay.finance.dto.response.GetWalletResponse;
import com.tinypay.finance.repository.BudgetPolicyRepository;
import com.tinypay.finance.repository.WalletRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final BudgetPolicyRepository budgetPolicyRepository;

    @Transactional(readOnly = true)
    public GetWalletResponse getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new CustomException(ErrorType.WALLET_NOT_FOUND));

        BudgetPolicy policy = budgetPolicyRepository.findByUser_IdAndDeletedAtIsNull(userId)
                .orElse(null);

        boolean autoPaymentEnabled = policy != null && policy.isAutoPaymentEnabled();

        return GetWalletResponse.builder()
                .walletAddress(wallet.getWalletAddress())
                .balance(wallet.getBalance())
                .walletStatus(wallet.getWalletStatus().name())
                .autoPaymentEnabled(autoPaymentEnabled)
                .build();
    }
}