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
    private static final long CODE_TTL_MINUTES = 3;
    private static final String REDIS_KEY_PREFIX = "sms:verify:";

    private final DefaultMessageService defaultMessageService;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${coolsms.from-number}")
    private String fromNumber;

    public void sendVerificationCode(String phoneNumber) {
        String code = generateCode();

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
    }

    public String getStoredCode(String phoneNumber) {
        return stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + phoneNumber);
    }

    public void deleteCode(String phoneNumber) {
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + phoneNumber);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}