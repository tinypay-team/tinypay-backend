package com.tinypay.dify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dify")
public record DifyProperties(
        String baseUrl,
        String chatAnalysisApiKey,
        String serviceExecutionApiKey
) {
}
