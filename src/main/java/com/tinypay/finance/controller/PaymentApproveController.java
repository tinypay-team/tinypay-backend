package com.tinypay.finance.controller;

import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import com.tinypay.finance.dto.request.PaymentApproveRequest;
import com.tinypay.finance.dto.response.PaymentApproveResponse;
import com.tinypay.finance.service.PaymentApproveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class PaymentApproveController {

    private final PaymentApproveService paymentApproveService;

    @PostMapping("/{requestId}/approval")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentApproveResponse> approvePayment(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long requestId,
            @RequestBody @Valid PaymentApproveRequest request) {
        return ApiResponse.success(SuccessType.PAYMENT_SUCCESS,
                paymentApproveService.paymentApprove(userId, requestId, request));
    }
}
