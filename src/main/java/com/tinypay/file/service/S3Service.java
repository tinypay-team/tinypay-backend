package com.tinypay.file.service;

import com.tinypay.chat.domain.FileAttachment;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.chat.repository.FileAttachmentRepository;
import com.tinypay.file.dto.ConfirmUploadRequest;
import com.tinypay.file.dto.ConfirmUploadResponse;
import com.tinypay.file.dto.PresignedDownloadUrlResponse;
import com.tinypay.file.dto.PresignedUploadUrlRequest;
import com.tinypay.file.dto.PresignedUploadUrlResponse;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ChatSessionRepository chatSessionRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final long EXPIRES_IN_SECONDS = 300; // 5분

    // 업로드용 Presigned URL 발급
    public PresignedUploadUrlResponse generateUploadUrl(Long userId, PresignedUploadUrlRequest request) {
        String storageKey = "uploads/" + userId + "/" + UUID.randomUUID() + "/" + request.fileName();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(request.fileType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + storageKey;

        log.info("[S3Service] 업로드 URL 발급: userId={}, storageKey={}", userId, storageKey);

        return new PresignedUploadUrlResponse(uploadUrl, storageKey, fileUrl, EXPIRES_IN_SECONDS);
    }

    // 다운로드용 Presigned URL 발급
    @Transactional(readOnly = true)
    public PresignedDownloadUrlResponse generateDownloadUrl(Long fileId) {
        FileAttachment file = fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorType.FILE_NOT_FOUND));

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(file.getStorageKey())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(EXPIRES_IN_SECONDS))
                .getObjectRequest(getObjectRequest)
                .build();

        String downloadUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        log.info("[S3Service] 다운로드 URL 발급: fileId={}, storageKey={}", fileId, file.getStorageKey());

        return new PresignedDownloadUrlResponse(
                downloadUrl,
                file.getFileName(),
                file.getFileType(),
                file.getFileSize(),
                EXPIRES_IN_SECONDS
        );
    }

    // 업로드 완료 후 DB에 파일 정보 저장
    @Transactional
    public ConfirmUploadResponse saveFileInfo(Long userId, ConfirmUploadRequest request) {
        ChatSession session = chatSessionRepository.findByIdAndUserId(request.sessionId(), userId)
                .orElseThrow(() -> new CustomException(ErrorType.CHAT_SESSION_NOT_FOUND));

        FileAttachment fileAttachment = FileAttachment.builder()
                .session(session)
                .fileName(request.fileName())
                .fileUrl("https://" + bucket + ".s3." + region + ".amazonaws.com/" + request.storageKey())
                .fileType(request.fileType())
                .fileSize(request.fileSize())
                .fileHash(UUID.randomUUID().toString()) // 실제로는 클라이언트에서 받아야 함
                .storageKey(request.storageKey())
                .build();

        FileAttachment saved = fileAttachmentRepository.save(fileAttachment);
        log.info("[S3Service] 파일 정보 저장 완료: fileId={}, storageKey={}", saved.getId(), saved.getStorageKey());

        return new ConfirmUploadResponse(
                saved.getId(),
                saved.getFileName(),
                saved.getFileType(),
                saved.getFileSize(),
                saved.getStorageKey()
        );
    }
}
