package com.tinypay.finance.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * 사용자 ID로 지갑 조회
     * Wallet → User 관계가 @ManyToOne(user_id)이므로 user.id로 조회
     */
    Optional<Wallet> findByUser_Id(Long userId);
}
