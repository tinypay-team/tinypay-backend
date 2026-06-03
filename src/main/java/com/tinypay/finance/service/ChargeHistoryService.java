package com.tinypay.finance.service;

import com.tinypay.finance.domain.ChargeHistory;
import com.tinypay.finance.domain.ChargeStatus;
import com.tinypay.finance.domain.Wallet;
import com.tinypay.finance.repository.ChargeHistoryRepository;
import com.tinypay.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ChargeHistoryService {

    private final ChargeHistoryRepository chargeHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedChargeHistory(User user, Wallet wallet, BigDecimal amount) {
        chargeHistoryRepository.save(ChargeHistory.builder()
                .user(user)
                .wallet(wallet)
                .amount(amount)
                .balanceSnapshot(wallet.getBalance())
                .chargeStatus(ChargeStatus.FAILED)
                .build());
    }
}