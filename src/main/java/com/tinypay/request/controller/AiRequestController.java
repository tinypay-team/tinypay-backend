package com.tinypay.request.controller;

import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import com.tinypay.request.dto.GetAiRequestStatusResponse;
import com.tinypay.request.service.AiRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/requests")
public class AiRequestController {

    private final AiRequestService aiRequestService;

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<GetAiRequestStatusResponse>> getAiRequestStatus(@RequestAttribute("userId") Long userId, @PathVariable Long requestId) {
        return ResponseEntity.ok(ApiResponse.success(SuccessType.GET_AI_REQUEST_STATUS_SUCCESS, aiRequestService.getAiRequestStatus(userId, requestId)));
    }
}
