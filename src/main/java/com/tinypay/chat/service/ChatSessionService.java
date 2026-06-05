package com.tinypay.chat.service;

import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.dto.CreateChatSessionResponse;
import com.tinypay.chat.dto.GetChatSessionResponse;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.chat.repository.FileAttachmentRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatSessionService {

    private static final String DEFAULT_TITLE = "새 채팅";

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateChatSessionResponse createChatSession(Long userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        ChatSession chatSession = ChatSession.builder()
                                      .user(user)
                                      .title(DEFAULT_TITLE)
                                      .build();

        ChatSession savedChatSession = chatSessionRepository.save(chatSession);

        return new CreateChatSessionResponse(savedChatSession.getId());
    }

    public List<GetChatSessionResponse> getChatSessions(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        return chatSessionRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(userId)
                   .stream()
                   .map(chatSession -> {
                       String preview = chatMessageRepository
                               .findTopBySessionIdOrderByCreatedAtDescIdDesc(chatSession.getId())
                               .map(msg -> {
                                   if (msg.getMessageType() == MessageType.FILE) {
                                       return fileAttachmentRepository
                                               .findByMessage_Id(msg.getId())
                                               .map(file -> file.getFileName())
                                               .orElse(null);
                                   }
                                   return msg.getContent();
                               })
                               .orElse(null);
                       return new GetChatSessionResponse(
                           chatSession.getId(),
                           chatSession.getTitle(),
                           preview,
                           chatSession.getCreatedAt().toLocalDate()
                       );
                   })
                   .toList();
    }
}