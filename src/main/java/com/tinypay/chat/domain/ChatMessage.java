package com.tinypay.chat.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "chat_message",
        indexes = @Index(name = "idx_chat_message_session_created_id", columnList = "session_id, created_at, id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "request_id")
    private AiRequest request;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private SenderRole senderRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Lob
    private String content;

    @Builder
    public ChatMessage(User user, ChatSession session, AiRequest request, SenderRole senderRole, MessageType messageType, String content
    ) {
        this.user = user;
        this.session = session;
        this.request = request;
        this.senderRole = senderRole == null ? SenderRole.USER : senderRole;
        this.messageType = messageType == null ? MessageType.TEXT : messageType;
        this.content = content;
    }

    public void connectRequest(AiRequest request) {
        this.request = request;
    }
}
