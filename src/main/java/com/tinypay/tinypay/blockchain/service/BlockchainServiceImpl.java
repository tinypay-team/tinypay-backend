package com.tinypay.tinypay.blockchain.service;

import com.tinypay.tinypay.blockchain.contracts.MockUSDC;
import com.tinypay.tinypay.blockchain.contracts.TinyPayment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class BlockchainServiceImpl implements BlockchainService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final MockUSDC mockUSDC;
    private final TinyPayment tinyPayment;

    private static final int USDC_DECIMALS = 6;

    public BlockchainServiceImpl(
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider,
            @Value("${blockchain.mock-usdc-address}") String mockUsdcAddress,
            @Value("${blockchain.tiny-payment-address}") String tinyPaymentAddress
    ) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.mockUSDC = MockUSDC.load(
                mockUsdcAddress, web3j, credentials, gasProvider);
        this.tinyPayment = TinyPayment.load(
                tinyPaymentAddress, web3j, credentials, gasProvider);
    }

    @Override
    public BigDecimal getBalance(String walletAddress) {
        try {
            BigInteger rawBalance = mockUSDC.balanceOf(walletAddress);
            return new BigDecimal(rawBalance)
                    .divide(BigDecimal.TEN.pow(USDC_DECIMALS));
        } catch (Exception e) {
            throw new RuntimeException("잔액 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String mintUsdc(String toWallet, BigInteger amount) {
        try {
            TransactionReceipt receipt = mockUSDC.mint(toWallet, amount);
            return receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("USDC 충전 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String transferUsdc(String orderId, String fromWallet,
                               String toWallet, BigInteger amount,
                               String serviceType) {
        try {
            TransactionReceipt receipt = tinyPayment.executePayment(
                    orderId, toWallet, amount, serviceType);
            return receipt.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("결제 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyReceipt(String txHash, String expectedReceiver,
                                 BigInteger expectedAmount) {
        // TODO: 영수증 5단계 검증 (Redis 연결 후 구현)
        return true;
    }
}
