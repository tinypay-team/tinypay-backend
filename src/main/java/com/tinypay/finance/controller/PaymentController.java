package com.tinypay.finance.controller;

import com.tinypay.finance.dto.response.GetPaymentDetailResponse;
import com.tinypay.finance.dto.response.GetPaymentListResponse;
import com.tinypay.finance.dto.response.AutoPaymentCheckResponse;
import com.tinypay.finance.service.PaymentService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/check")
    public ApiResponse<AutoPaymentCheckResponse> checkAutoPayment(
            @RequestAttribute("userId") Long userId,
            @RequestParam BigDecimal estimatedCost) {
        return ApiResponse.success(SuccessType.GET_AUTO_PAYMENT_CHECK_SUCCESS, paymentService.checkAutoPayment(userId, estimatedCost));
    }

    @GetMapping
    public ApiResponse<GetPaymentListResponse> getPaymentList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long cursor) {
        return ApiResponse.success(SuccessType.GET_PAYMENT_LIST_SUCCESS, paymentService.getPaymentList(userId, cursor));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<GetPaymentDetailResponse> getPaymentDetail(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long paymentId) {
        return ApiResponse.success(SuccessType.GET_PAYMENT_DETAIL_SUCCESS, paymentService.getPaymentDetail(userId, paymentId));
    }
}