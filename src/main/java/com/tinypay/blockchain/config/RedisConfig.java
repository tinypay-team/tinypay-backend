package com.tinypay.blockchain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 설정
 *
 * StringRedisTemplate을 명시적으로 Bean 등록하여,
 * 키/값을 모두 String으로 직렬화하도록 보장한다.
 *
 * 사용 예:
 *   stringRedisTemplate.opsForValue().set("tx:used:0xabc", "used");
 *   String result = stringRedisTemplate.opsForValue().get("tx:used:0xabc");
 */
@Configuration
public class RedisConfig {

    /**
     * String 타입 전용 RedisTemplate.
     *
     * 영수증 재사용 검사 용도:
     *   - Key: "tx:used:{tx_hash}"
     *   - Value: "used" (또는 사용 시각 등)
     *
     * @param connectionFactory Spring Boot가 application.yml의 spring.data.redis 설정으로
     *                          자동 생성한 ConnectionFactory를 주입받음
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
