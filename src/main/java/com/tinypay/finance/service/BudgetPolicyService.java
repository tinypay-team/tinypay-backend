package com.tinypay.finance.service;

import com.tinypay.finance.domain.BudgetPolicy;
import com.tinypay.finance.dto.request.UpdatePerPaymentLimitRequest;
import com.tinypay.finance.dto.response.UpdatePerPaymentLimitResponse;
import com.tinypay.finance.repository.BudgetPolicyRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BudgetPolicyService {

    private final UserRepository userRepository;
    private final BudgetPolicyRepository budgetPolicyRepository;

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
}