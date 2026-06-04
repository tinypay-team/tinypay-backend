package com.tinypay.auth.service;

import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final int CODE_LENGTH = 6;
    private static final long CODE_TTL_MINUTES = 5;
    private static final long ATTEMPTS_TTL_DAYS = 7;
    private static final int MAX_SEND_COUNT = 10;
    private static final long SEND_COUNT_TTL_HOURS = 24;
    private static final long COOLDOWN_SECONDS = 60;
    private static final String REDIS_KEY_PREFIX = "sms:verify:";
    private static final String ATTEMPTS_KEY_PREFIX = "sms:attempts:";
    private static final String BLOCKED_KEY_PREFIX = "sms:blocked:";
    private static final String COOLDOWN_KEY_PREFIX = "sms:cooldown:";
    private static final String SEND_COUNT_KEY_PREFIX = "sms:send-count:";

    private final DefaultMessageService defaultMessageService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${coolsms.from-number}")
    private String fromNumber;

    public void sendVerificationCode(String phoneNumber) {
        checkSendLimit(phoneNumber);
        checkCooldown(phoneNumber);

        String code = generateCode();

        stringRedisTemplate.delete(REDIS_KEY_PREFIX + phoneNumber);
        stringRedisTemplate.opsForValue().set(
                REDIS_KEY_PREFIX + phoneNumber,
                code,
                Duration.ofMinutes(CODE_TTL_MINUTES)
        );

        try {
            Message message = new Message();
            message.setFrom(fromNumber);
            message.setTo(phoneNumber);
            message.setText("[TinyPay] 인증번호: " + code);
            defaultMessageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            stringRedisTemplate.delete(REDIS_KEY_PREFIX + phoneNumber);
            throw new CustomException(ErrorType.SMS_SEND_FAILED);
        }

        stringRedisTemplate.opsForValue().set(
                COOLDOWN_KEY_PREFIX + phoneNumber, "1", Duration.ofSeconds(COOLDOWN_SECONDS));
        Long sendCount = stringRedisTemplate.opsForValue().increment(SEND_COUNT_KEY_PREFIX + phoneNumber);
        if (sendCount == 1) {
            stringRedisTemplate.expire(SEND_COUNT_KEY_PREFIX + phoneNumber, Duration.ofHours(SEND_COUNT_TTL_HOURS));
        }
    }

    private void checkCooldown(String phoneNumber) {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(COOLDOWN_KEY_PREFIX + phoneNumber))) {
            throw new CustomException(ErrorType.VERIFICATION_CODE_COOLDOWN);
        }
    }

    private void checkSendLimit(String phoneNumber) {
        String countStr = stringRedisTemplate.opsForValue().get(SEND_COUNT_KEY_PREFIX + phoneNumber);
        if (countStr != null && Long.parseLong(countStr) >= MAX_SEND_COUNT) {
            throw new CustomException(ErrorType.VERIFICATION_SEND_LIMIT_EXCEEDED);
        }
    }

    public String getStoredCode(String phoneNumber) {
        return stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + phoneNumber);
    }

    public void deleteCode(String phoneNumber) {
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + phoneNumber);
    }

    public boolean isBlocked(String phoneNumber) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLOCKED_KEY_PREFIX + phoneNumber));
    }

    public String getBlockMessage(String phoneNumber) {
        return stringRedisTemplate.opsForValue().get(BLOCKED_KEY_PREFIX + phoneNumber);
    }

    public String handleFailedAttempt(String phoneNumber) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX + phoneNumber;
        Long attempts = stringRedisTemplate.opsForValue().increment(attemptsKey);
        stringRedisTemplate.expire(attemptsKey, Duration.ofDays(ATTEMPTS_TTL_DAYS));

        String blockMessage = resolveBlockMessage(attempts);
        if (blockMessage != null) {
            Duration blockDuration = resolveBlockDuration(attempts);
            stringRedisTemplate.opsForValue().set(
                    BLOCKED_KEY_PREFIX + phoneNumber,
                    blockMessage,
                    blockDuration
            );
            stringRedisTemplate.delete(REDIS_KEY_PREFIX + phoneNumber);
        }
        return blockMessage;
    }

    private String resolveBlockMessage(Long attempts) {
        if (attempts == null) return null;
        if (attempts >= 10) return "인증 시도 횟수를 초과했습니다. 24시간 후 다시 시도해주세요.";
        if (attempts >= 8) return "인증 시도 횟수를 초과했습니다. 1시간 후 다시 시도해주세요.";
        if (attempts >= 5) return "인증 시도 횟수를 초과했습니다. 30분 후 다시 시도해주세요.";
        return null;
    }

    private Duration resolveBlockDuration(Long attempts) {
        if (attempts == null) return Duration.ofMinutes(30);
        if (attempts >= 10) return Duration.ofHours(24);
        if (attempts >= 8) return Duration.ofHours(1);
        return Duration.ofMinutes(30);
    }

    public void clearAttempts(String phoneNumber) {
        stringRedisTemplate.delete(ATTEMPTS_KEY_PREFIX + phoneNumber);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}