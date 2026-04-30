package com.tinypay.tinypay.blockchain.service;

import com.tinypay.tinypay.blockchain.contracts.MockUSDC;
import com.tinypay.tinypay.blockchain.contracts.TinyPayment;
import com.tinypay.tinypay.blockchain.exception.InsufficientPaymentException;
import com.tinypay.tinypay.blockchain.exception.InvalidContractException;
import com.tinypay.tinypay.blockchain.exception.InvalidRecipientException;
import com.tinypay.tinypay.blockchain.exception.ReplayAttackException;
import com.tinypay.tinypay.blockchain.exception.TransactionFailedException;
import com.tinypay.tinypay.blockchain.verification.ReceiptVerifier;
import com.tinypay.tinypay.blockchain.verification.VerificationResult;
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
    private final ReceiptVerifier receiptVerifier;   

    private static final int USDC_DECIMALS = 6;

    public BlockchainServiceImpl(
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider gasProvider,
            ReceiptVerifier receiptVerifier,                                               // ← 파라미터 추가
            @Value("${blockchain.mock-usdc-address}") String mockUsdcAddress,
            @Value("${blockchain.tiny-payment-address}") String tinyPaymentAddress
    ) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.receiptVerifier = receiptVerifier;                                            // ← 본문 추가
        this.mockUSDC = MockUSDC.load(
                mockUsdcAddress, web3j, credentials, gasProvider);
        this.tinyPayment = TinyPayment.load(
                tinyPaymentAddress, web3j, credentials, gasProvider);
    }

    @Override
    public BigDecimal getBalance(String walletAddress) {
        try {
            BigInteger rawBalance = mockUSDC.balanceOf(walletAddress).send();
            return new BigDecimal(rawBalance)
                    .divide(BigDecimal.TEN.pow(USDC_DECIMALS));
        } catch (Exception e) {
            throw new RuntimeException("잔액 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String mintUsdc(String toWallet, BigInteger amount) {
        try {
            TransactionReceipt receipt = mockUSDC.mint(toWallet, amount).send();
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
        VerificationResult result = receiptVerifier.verify(txHash, expectedReceiver, expectedAmount);

        if (result.isValid()) {
            return true;
        }

        // 실패 사유별로 명세서 7번에 정의된 예외 throw
        String detail = result.getDetail();
        switch (result.getReason()) {
            case REPLAY_ATTACK:
                throw new ReplayAttackException(detail);
            case TRANSACTION_FAILED:
                throw new TransactionFailedException(detail);
            case INVALID_CONTRACT:
                throw new InvalidContractException(detail);
            case INVALID_RECIPIENT:
                throw new InvalidRecipientException(detail);
            case INSUFFICIENT_PAYMENT:
                throw new InsufficientPaymentException(detail);
            default:
                // SUCCESS는 위에서 이미 처리됐으므로 여기 도달 불가
                throw new IllegalStateException("불가능한 검증 상태: " + result.getReason());
        }
    }
}
