package com.tinypay.global.config;

import com.tinypay.auth.jwt.JwtTokenProvider;
import com.tinypay.chat.domain.ChatMessage;
import com.tinypay.chat.domain.ChatSession;
import com.tinypay.chat.domain.MessageType;
import com.tinypay.chat.domain.SenderRole;
import com.tinypay.chat.repository.ChatMessageRepository;
import com.tinypay.chat.repository.ChatSessionRepository;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("performance")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "performance.seed.enabled", havingValue = "true")
public class PerformanceDataInitializer implements ApplicationRunner {

    private static final int MESSAGE_COUNT = 200;

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByProviderId("performance-user")
                .orElseGet(() -> userRepository.save(User.builder()
                        .providerId("performance-user")
                        .email("performance@tinypay.local")
                        .nickname("performance")
                        .build()));

        ChatSession session = chatSessionRepository.save(ChatSession.builder()
                .user(user)
                .title("Performance Test")
                .build());

        List<ChatMessage> messages = new ArrayList<>(MESSAGE_COUNT);
        for (int index = 1; index <= MESSAGE_COUNT; index++) {
            SenderRole role = index % 2 == 0 ? SenderRole.ASSISTANT : SenderRole.USER;
            messages.add(ChatMessage.builder()
                    .user(role == SenderRole.USER ? user : null)
                    .session(session)
                    .senderRole(role)
                    .messageType(MessageType.TEXT)
                    .content("performance message " + index)
                    .build());
        }
        chatMessageRepository.saveAll(messages);

        String token = jwtTokenProvider.generateAccessToken(user.getId());
        log.info("PERFORMANCE_TEST_READY BASE_URL=http://host.docker.internal:8080 SESSION_ID={} ACCESS_TOKEN={}",
                session.getId(), token);
    }
}
