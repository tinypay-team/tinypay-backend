package com.tinypay.finance.service;

import com.tinypay.finance.domain.PaymentLog;
import com.tinypay.finance.dto.response.GetPaymentListResponse;
import com.tinypay.finance.repository.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final int PAGE_SIZE = 10;

    private final PaymentLogRepository paymentLogRepository;

    @Transactional(readOnly = true)
    public GetPaymentListResponse getPaymentList(Long userId, Long cursor) {
        List<PaymentLog> logs = cursor == null
                ? paymentLogRepository.findTop10ByUser_IdOrderByIdDesc(userId)
                : paymentLogRepository.findTop10ByUser_IdAndIdLessThanOrderByIdDesc(userId, cursor);

        Long nextCursor = logs.size() == PAGE_SIZE ? logs.get(logs.size() - 1).getId() : null;

        return GetPaymentListResponse.builder()
                .payments(logs.stream()
                        .map(log -> GetPaymentListResponse.PaymentItem.builder()
                                .paymentId(log.getId())
                                .serviceName(log.getRequest().getServiceName())
                                .paidAmount(log.getAmount())
                                .executedAt(log.getExecutedAt())
                                .paymentStatus(log.getPaymentStatus().name())
                                .build())
                        .toList())
                .nextCursor(nextCursor)
                .build();
    }
}