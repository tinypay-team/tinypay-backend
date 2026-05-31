package com.tinypay.chat.domain;

import com.tinypay.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "file")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileAttachment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "message_id")
    private ChatMessage message;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_hash", nullable = false)
    private String fileHash;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Builder
    public FileAttachment(ChatSession session, ChatMessage message, String fileName, String fileUrl, String fileType, Long fileSize, String fileHash, String storageKey
    ) {
        this.session = session;
        this.message = message;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.storageKey = storageKey;
    }

    public void connectMessage(ChatMessage message) {
        this.message = message;
    }
}
