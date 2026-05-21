package com.tinypay.chat.repository;

import com.tinypay.request.domain.AiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRequestRepository extends JpaRepository<AiRequest, Long> {
}
