package com.tinypay.chat.repository;

import com.tinypay.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    long countBySessionId(Long sessionId);

    java.util.Optional<ChatMessage> findTopBySessionIdOrderByCreatedAtDescIdDesc(Long sessionId);
}
