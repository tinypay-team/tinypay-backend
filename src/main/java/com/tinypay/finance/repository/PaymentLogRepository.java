package com.tinypay.finance.repository;

import com.tinypay.finance.domain.PaymentLog;
import com.tinypay.finance.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentLog p " +
            "WHERE p.user.id = :userId " +
            "AND p.paymentStatus = :status " +
            "AND YEAR(p.executedAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(p.executedAt) = MONTH(CURRENT_DATE)")
    BigDecimal sumSuccessfulAmountThisMonth(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    List<PaymentLog> findTop3ByUser_IdAndPaymentStatusOrderByExecutedAtDesc(Long userId, PaymentStatus status);

    @Query("SELECT COUNT(p) FROM PaymentLog p " +
            "WHERE p.user.id = :userId " +
            "AND p.paymentStatus = :status " +
            "AND YEAR(p.executedAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(p.executedAt) = MONTH(CURRENT_DATE)")
    long countSuccessfulThisMonth(@Param("userId") Long userId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(AVG(p.amount), 0) FROM PaymentLog p " +
            "WHERE p.user.id = :userId " +
            "AND p.paymentStatus = :status " +
            "AND YEAR(p.executedAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(p.executedAt) = MONTH(CURRENT_DATE)")
    BigDecimal averageSuccessfulAmountThisMonth(@Param("userId") Long userId, @Param("status") PaymentStatus status);
}
