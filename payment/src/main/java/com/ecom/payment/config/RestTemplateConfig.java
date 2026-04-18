package com.ecom.payment.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Build via RestTemplateBuilder so Spring Boot can auto-apply Micrometer Observation/Tracing
     * customizers (creates client spans for outbound HTTP calls).
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
