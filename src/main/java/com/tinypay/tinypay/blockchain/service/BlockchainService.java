package com.tinypay.tinypay.blockchain.service;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface BlockchainService {

    // 잔액 조회
    BigDecimal getBalance(String walletAddress);

    // USDC 전송 (결제)
    String transferUsdc(String orderId, String fromWallet,
                        String toWallet, BigInteger amount,
                        String serviceType);

    // USDC 발행 (충전)
    String mintUsdc(String toWallet, BigInteger amount);

    // 영수증 5단계 검증
    boolean verifyReceipt(String txHash, String expectedReceiver,
                          BigInteger expectedAmount);
}
