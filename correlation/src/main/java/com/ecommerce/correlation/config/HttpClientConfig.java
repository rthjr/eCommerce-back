package com.ecommerce.correlation.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(CorrelationProperties.class)
public class HttpClientConfig {

    @Bean
    @Qualifier("prometheusRestClient")
    RestClient prometheusRestClient(RestClient.Builder builder, CorrelationProperties properties) {
        return buildClient(builder, properties.getPrometheus().getBaseUrl(), properties.getBackendTimeout());
    }

    @Bean
    @Qualifier("lokiRestClient")
    RestClient lokiRestClient(RestClient.Builder builder, CorrelationProperties properties) {
        return buildClient(builder, properties.getLoki().getBaseUrl(), properties.getBackendTimeout());
    }

    @Bean
    @Qualifier("zipkinRestClient")
    RestClient zipkinRestClient(RestClient.Builder builder, CorrelationProperties properties) {
        return buildClient(builder, properties.getZipkin().getBaseUrl(), properties.getBackendTimeout());
    }

    private RestClient buildClient(RestClient.Builder builder, String baseUrl, Duration timeout) {
        var timeoutMs = (int) Math.min(Integer.MAX_VALUE, timeout.toMillis());
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        return builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
