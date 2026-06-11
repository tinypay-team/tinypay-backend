package com.tinypay.dify.repository;

import com.tinypay.dify.domain.AiRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AiRequestRepository extends JpaRepository<AiRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM AiRequest r WHERE r.id = :id")
    Optional<AiRequest> findByIdWithLock(@Param("id") Long id);
}
