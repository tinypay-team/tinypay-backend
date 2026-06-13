package com.tinypay.chat.repository;

import com.tinypay.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = "request")
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<ChatMessage> findTop10BySessionIdOrderByCreatedAtDescIdDesc(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    java.util.Optional<ChatMessage> findTopBySessionIdOrderByCreatedAtDescIdDesc(Long sessionId);
}
