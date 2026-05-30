package com.tinypay.finance.service;

import com.tinypay.blockchain.service.BlockchainService;
import com.tinypay.finance.domain.*;
import com.tinypay.finance.repository.BudgetPolicyRepository;
import com.tinypay.finance.repository.PaymentLogRepository;
import com.tinypay.finance.repository.WalletRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestStatus;
import com.tinypay.finance.dto.request.PaymentApproveRequest;
import com.tinypay.finance.dto.response.PaymentApproveResponse;
import com.tinypay.dify.repository.AiRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentApproveService {

    private final AiRequestRepository aiRequestRepository;
    private final WalletRepository walletRepository;
    private final BudgetPolicyRepository budgetPolicyRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentLogService paymentLogService;
    private final BlockchainService blockchainService;

    @Value("${blockchain.server-wallet.address}")
    private String receiverWalletAddress;

    private static final int USDC_DECIMALS = 6;

    @Transactional
    public PaymentApproveResponse paymentApprove(Long userId, Long requestId, PaymentApproveRequest request) {
        // 1. AI 요청 조회
        AiRequest aiRequest = aiRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorType.AI_REQUEST_NOT_FOUND));

        // 2. 소유자 확인
        if (!aiRequest.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorType.REQUEST_FORBIDDEN);
        }

        // 3. 상태 확인
        if (aiRequest.getStatus() != AiRequestStatus.WAITING_APPROVAL) {
            throw new CustomException(ErrorType.INVALID_REQUEST_STATUS);
        }

        // 4. 예상 금액 일치 확인
        if (aiRequest.getEstimatedTotalCost().compareTo(request.getEstimatedCost()) != 0) {
            throw new CustomException(ErrorType.ESTIMATED_COST_MISMATCH);
        }

        BigDecimal estimatedCost = request.getEstimatedCost();

        // 5. 지갑 조회
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() -> new CustomException(ErrorType.WALLET_NOT_FOUND));

        // 6. 지갑 상태 확인
        if (wallet.getWalletStatus() == WalletStatus.LOCKED) {
            throw new CustomException(ErrorType.WALLET_LOCKED);
        }

        // 7. 잔액 확인
        if (wallet.getBalance().compareTo(estimatedCost) < 0) {
            throw new CustomException(ErrorType.INSUFFICIENT_BALANCE);
        }

        // 8. 예산 정책 확인
        budgetPolicyRepository.findByUser_IdAndDeletedAtIsNull(userId).ifPresent(policy -> {
            if (policy.getPerRequestLimit() != null &&
                    estimatedCost.compareTo(policy.getPerRequestLimit()) > 0) {
                throw new CustomException(ErrorType.PER_REQUEST_LIMIT_EXCEEDED);
            }
            if (policy.getMonthlyLimit() != null) {
                BigDecimal monthlySpent = paymentLogRepository
                        .sumSuccessfulAmountThisMonth(userId, PaymentStatus.SUCCESS);
                if (monthlySpent.add(estimatedCost).compareTo(policy.getMonthlyLimit()) > 0) {
                    throw new CustomException(ErrorType.MONTHLY_LIMIT_EXCEEDED);
                }
            }
        });

        // 9. 블록체인 결제 실행
        String orderId = UUID.randomUUID().toString();
        BigInteger rawAmount = estimatedCost.movePointRight(USDC_DECIMALS).toBigInteger();

        String txHash;
        try {
            txHash = blockchainService.transferUsdc(
                    orderId,
                    wallet.getWalletAddress(),
                    receiverWalletAddress,
                    rawAmount,
                    "AI_SERVICE"
            );
        } catch (Exception e) {
            paymentLogService.saveFailedPaymentLog(
                    aiRequest.getUser(), aiRequest, wallet, orderId, receiverWalletAddress, estimatedCost);
            throw new CustomException(ErrorType.INTERNAL_SERVER_ERROR);
        }

        // 10. 결제 기록 저장
        LocalDateTime executedAt = LocalDateTime.now();
        PaymentLog paymentLog = PaymentLog.builder()
                .user(aiRequest.getUser())
                .request(aiRequest)
                .wallet(wallet)
                .orderId(orderId)
                .txHash(txHash)
                .payerWalletAddress(wallet.getWalletAddress())
                .receiverWalletAddress(receiverWalletAddress)
                .amount(estimatedCost)
                .paymentStatus(PaymentStatus.SUCCESS)
                .executedAt(executedAt)
                .blockchainNetwork(wallet.getBlockchainNetwork())
                .build();
        paymentLogRepository.save(paymentLog);

        // 11. 지갑 잔액 차감
        wallet.updateBalance(wallet.getBalance().subtract(estimatedCost));

        // 12. 요청 상태 업데이트
        aiRequest.approve();
        aiRequest.startExecution();
        aiRequest.complete();

        return PaymentApproveResponse.builder()
                .requestId(aiRequest.getId())
                .status(aiRequest.getStatus().name())
                .payment(PaymentApproveResponse.PaymentInfo.builder()
                        .paymentId(paymentLog.getId())
                        .orderId(paymentLog.getOrderId())
                        .transactionHash(txHash)
                        .amount(estimatedCost)
                        .executedAt(executedAt)
                        .build())
                .wallet(PaymentApproveResponse.WalletInfo.builder()
                        .balance(wallet.getBalance())
                        .build())
                .build();
    }
}