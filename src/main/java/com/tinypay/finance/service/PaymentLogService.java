package com.tinypay.finance.service;

import com.tinypay.dify.domain.AiRequest;
import com.tinypay.finance.domain.*;
import com.tinypay.finance.repository.PaymentLogRepository;
import com.tinypay.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedPaymentLog(User user, AiRequest aiRequest,
                                     Wallet wallet, String orderId, String receiverWalletAddress,
                                     BigDecimal amount) {
        paymentLogRepository.save(PaymentLog.builder()
                .user(user)
                .request(aiRequest)
                .wallet(wallet)
                .orderId(orderId)
                .txHash("FAILED_" + orderId)
                .payerWalletAddress(wallet.getWalletAddress())
                .receiverWalletAddress(receiverWalletAddress)
                .amount(amount)
                .paymentStatus(PaymentStatus.FAILED)
                .verificationStatus(VerificationStatus.FAILED)
                .executedAt(LocalDateTime.now())
                .blockchainNetwork(wallet.getBlockchainNetwork())
                .build());
    }
}