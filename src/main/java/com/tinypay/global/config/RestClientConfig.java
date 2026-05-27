package com.tinypay.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// Dify, LLM API, 외부 API 같은 다른 서버를 호출하기 위해 RestClient를 스프링 Bean으로 등록하는 설정 코드
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
