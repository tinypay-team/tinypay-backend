package com.tinypay.file.controller;

import com.tinypay.file.dto.ConfirmUploadRequest;
import com.tinypay.file.dto.ConfirmUploadResponse;
import com.tinypay.file.dto.PresignedDownloadUrlResponse;
import com.tinypay.file.dto.PresignedUploadUrlRequest;
import com.tinypay.file.dto.PresignedUploadUrlResponse;
import com.tinypay.file.service.S3Service;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final S3Service s3Service;


    @PostMapping("/presigned-url/upload")
    public ResponseEntity<ApiResponse<PresignedUploadUrlResponse>> getUploadPresignedUrl(@RequestAttribute("userId") Long userId, @Valid @RequestBody PresignedUploadUrlRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(SuccessType.GET_UPLOAD_PRESIGNED_URL_SUCCESS, s3Service.generateUploadUrl(userId, request)));
    }


    @PostMapping("/confirm-upload")
    public ResponseEntity<ApiResponse<ConfirmUploadResponse>> confirmUpload(@RequestAttribute("userId") Long userId, @Valid @RequestBody ConfirmUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(SuccessType.CONFIRM_UPLOAD_SUCCESS, s3Service.saveFileInfo(userId, request)));
    }


    @GetMapping("/{fileId}/presigned-url/download")
    public ResponseEntity<ApiResponse<PresignedDownloadUrlResponse>> getDownloadPresignedUrl(@PathVariable Long fileId) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(SuccessType.GET_DOWNLOAD_PRESIGNED_URL_SUCCESS, s3Service.generateDownloadUrl(fileId)));
    }
}
