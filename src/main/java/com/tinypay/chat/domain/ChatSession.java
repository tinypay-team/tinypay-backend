package com.tinypay.chat.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import com.tinypay.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Builder
    public ChatSession(User user, String title) {
        this.user = user;
        this.title = title == null ? "새 채팅" : title;
    }

    public void updateTitle(String title) {
        if (title == null || title.isBlank()) {
            return;
        }
        this.title = title;
    }
}
