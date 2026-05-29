package com.tinypay.finance.repository;

import com.tinypay.finance.domain.PaymentLog;
import com.tinypay.finance.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentLog p " +
            "WHERE p.user.id = :userId " +
            "AND p.paymentStatus = :status " +
            "AND YEAR(p.executedAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(p.executedAt) = MONTH(CURRENT_DATE)")
    BigDecimal sumSuccessfulAmountThisMonth(@Param("userId") Long userId, @Param("status") PaymentStatus status);
}
