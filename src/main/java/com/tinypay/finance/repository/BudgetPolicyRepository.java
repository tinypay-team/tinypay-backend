package com.tinypay.finance.repository;

import com.tinypay.finance.domain.BudgetPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetPolicyRepository extends JpaRepository<BudgetPolicy, Long> {

    Optional<BudgetPolicy> findByUser_IdAndDeletedAtIsNull(Long userId);
}
