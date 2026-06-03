package com.tinypay.finance.repository;

import com.tinypay.finance.domain.ChargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, Long> {
}