package com.tinypay.dify.repository;

import com.tinypay.dify.domain.AiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestRepository extends JpaRepository<AiRequest, Long> {
}
