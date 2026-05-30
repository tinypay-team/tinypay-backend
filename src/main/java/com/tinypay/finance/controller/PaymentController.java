package com.tinypay.finance.controller;

import com.tinypay.finance.dto.response.GetPaymentListResponse;
import com.tinypay.finance.service.PaymentService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ApiResponse<GetPaymentListResponse> getPaymentList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long cursor) {
        return ApiResponse.success(SuccessType.GET_PAYMENT_LIST_SUCCESS, paymentService.getPaymentList(userId, cursor));
    }
}