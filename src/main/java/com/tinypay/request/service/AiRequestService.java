package com.tinypay.request.service;

import com.tinypay.dify.repository.AiRequestRepository;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.request.dto.AiRequestResponseStatus;
import com.tinypay.request.dto.GetAiRequestStatusResponse;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiRequestService {

    private final AiRequestRepository aiRequestRepository;
    private final UserRepository userRepository;

    public GetAiRequestStatusResponse getAiRequestStatus(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        AiRequest aiRequest = aiRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorType.AI_REQUEST_NOT_FOUND));

        if (!aiRequest.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorType.REQUEST_FORBIDDEN);
        }

        return new GetAiRequestStatusResponse(aiRequest.getId(), aiRequest.getSession().getId(), AiRequestResponseStatus.from(aiRequest.getStatus())
        );
    }
}
