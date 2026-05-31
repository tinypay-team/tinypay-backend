package com.tinypay.request.service;

import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.dify.repository.AiRequestRepository;
import com.tinypay.dify.domain.AiRequest;
import com.tinypay.dify.domain.AiRequestStatus;
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
    private final ChatMessageRepository chatMessageRepository;
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

    @Transactional
    public void cancelAiRequest(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        AiRequest aiRequest = aiRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorType.AI_REQUEST_NOT_FOUND));

        if (!aiRequest.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorType.REQUEST_FORBIDDEN);
        }

        if (aiRequest.getStatus() != AiRequestStatus.WAITING_APPROVAL) {
            throw new CustomException(ErrorType.CANCEL_NOT_ALLOWED);
        }

        aiRequest.cancel();

        chatMessageRepository.save(
                ChatMessage.builder()
                        .session(aiRequest.getSession())
                        .senderRole(SenderRole.ASSISTANT)
                        .messageType(MessageType.TEXT)
                        .content("결제 요청이 취소되었습니다.")
                        .build()
        );
    }
}
