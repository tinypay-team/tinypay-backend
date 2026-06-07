package com.tinypay.finance.service;

import com.tinypay.abuse.service.AbuseService;
import com.tinypay.blockchain.service.BlockchainService;
import com.tinypay.finance.domain.BudgetPolicy;
import com.tinypay.finance.domain.Wallet;
import com.tinypay.finance.domain.WalletStatus;
import com.tinypay.finance.dto.request.UpdateAutoPaymentRequest;
import com.tinypay.finance.dto.request.UpdateMonthlyBudgetRequest;
import com.tinypay.finance.dto.request.UpdatePerPaymentLimitRequest;
import com.tinypay.finance.dto.response.UpdateAutoPaymentResponse;
import com.tinypay.finance.dto.response.UpdateMonthlyBudgetResponse;
import com.tinypay.finance.dto.response.UpdatePerPaymentLimitResponse;
import com.tinypay.finance.repository.BudgetPolicyRepository;
import com.tinypay.finance.repository.WalletRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BudgetPolicyService {

    private final AbuseService abuseService;
    private final UserRepository userRepository;
    private final BudgetPolicyRepository budgetPolicyRepository;
    private final WalletRepository walletRepository;
    private final BlockchainService blockchainService;
    private final StringRedisTemplate stringRedisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final int MAX_PASSWORD_FAILURES = 5;
    private static final String PASSWORD_FAIL_KEY_PREFIX = "wallet:password:fail:";

    @Transactional
    public UpdatePerPaymentLimitResponse updatePerPaymentLimit(Long userId, UpdatePerPaymentLimitRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        BudgetPolicy policy = budgetPolicyRepository.findByUser_IdAndDeletedAtIsNull(userId)
                .orElseGet(() -> budgetPolicyRepository.save(
                        BudgetPolicy.builder()
                                .user(user)
                                .autoPaymentEnabled(false)
                                .build()
                ));

        policy.updatePerRequestLimit(request.getPerPaymentLimit());

        return UpdatePerPaymentLimitResponse.builder()
                .perPaymentLimit(policy.getPerRequestLimit())
                .build();
    }

    @Transactional
    public UpdateMonthlyBudgetResponse updateMonthlyBudget(Long userId, UpdateMonthlyBudgetRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        BudgetPolicy policy = budgetPolicyRepository.findByUser_IdAndDeletedAtIsNull(userId)
                .orElseGet(() -> budgetPolicyRepository.save(
                        BudgetPolicy.builder()
                                .user(user)
                                .autoPaymentEnabled(false)
                                .build()
                ));

        policy.updateMonthlyLimit(request.getMonthlyBudget());

        return UpdateMonthlyBudgetResponse.builder()
                .monthlyBudget(policy.getMonthlyLimit())
                .build();
    }

    @Transactional
    public UpdateAutoPaymentResponse updateAutoPayment(Long userId, Long walletId, UpdateAutoPaymentRequest request) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new CustomException(ErrorType.WALLET_NOT_FOUND));

        if (!wallet.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorType.WALLET_FORBIDDEN);
        }

        if (wallet.getWalletStatus() == WalletStatus.LOCKED) {
            throw new CustomException(ErrorType.WALLET_LOCKED);
        }

        if (request.getEnabled()) {
            if (!StringUtils.hasText(request.getWalletPassword())) {
                throw new CustomException(ErrorType.AUTO_PAYMENT_PASSWORD_REQUIRED);
            }
            verifyWalletPassword(userId, request.getWalletPassword(), wallet);
        }

        BudgetPolicy policy = budgetPolicyRepository.findByUser_IdAndDeletedAtIsNull(userId)
                .orElseGet(() -> budgetPolicyRepository.save(
                        BudgetPolicy.builder()
                                .user(wallet.getUser())
                                .autoPaymentEnabled(false)
                                .build()
                ));

        policy.updateAutoPaymentEnabled(request.getEnabled());

        return UpdateAutoPaymentResponse.builder()
                .autoPaymentEnabled(policy.isAutoPaymentEnabled())
                .build();
    }

    private void verifyWalletPassword(Long userId, String inputPassword, Wallet wallet) {
        String failKey = PASSWORD_FAIL_KEY_PREFIX + userId;

        if (!passwordEncoder.matches(inputPassword, wallet.getWalletPassword())) {
            Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
            stringRedisTemplate.expire(failKey, Duration.ofHours(24));

            if (failCount != null && failCount >= MAX_PASSWORD_FAILURES) {
                abuseService.recordRateLimitViolation(userId, "자동결제 활성화 비밀번호 " + MAX_PASSWORD_FAILURES + "회 실패: userId=" + userId);
                blockchainService.lockWalletForBruteForce(userId);
                stringRedisTemplate.delete(failKey);
                throw new CustomException(ErrorType.WALLET_LOCKED_BY_PASSWORD_FAILURE);
            }

            throw new CustomException(ErrorType.WRONG_WALLET_PASSWORD);
        }

        stringRedisTemplate.delete(failKey);
    }
}