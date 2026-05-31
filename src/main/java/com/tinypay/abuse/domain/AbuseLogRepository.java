package com.tinypay.abuse.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AbuseLogRepository extends JpaRepository<AbuseLog, Long> {
}
