package com.ecommerce.correlation.client;

import com.ecommerce.correlation.config.CorrelationProperties;
import com.ecommerce.correlation.exception.BackendQueryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Optional;

@Component
public class PrometheusClient {
    private final RestClient prometheusRestClient;
    private final CorrelationProperties properties;
    private final ObjectMapper objectMapper;

    public PrometheusClient(
            @Qualifier("prometheusRestClient") RestClient prometheusRestClient,
            CorrelationProperties properties,
            ObjectMapper objectMapper
    ) {
        this.prometheusRestClient = prometheusRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public Optional<String> queryMetricValue(String service) {
        String query = getRenderedQuery(service);

        try {
            String response = prometheusRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/query")
                            .queryParam("query", query)
                            .queryParam("time", Instant.now().getEpochSecond())
                            .build())
                    .retrieve()
                    .body(String.class);

            return parseValue(response);
        } catch (RestClientException ex) {
            throw new BackendQueryException("prometheus", "Failed to query Prometheus metric", ex);
        } catch (JsonProcessingException ex) {
            throw new BackendQueryException("prometheus", "Failed to parse Prometheus response", ex);
        }
    }

    public String getRenderedQuery(String service) {
        return properties.getPrometheus().getMetricQueryTemplate()
                .formatted(service.replace("\"", "\\\""));
    }

    private Optional<String> parseValue(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        JsonNode result = root.path("data").path("result");

        if (!result.isArray() || result.isEmpty()) {
            return Optional.empty();
        }

        JsonNode valueNode = result.get(0).path("value");
        if (!valueNode.isArray() || valueNode.size() < 2) {
            return Optional.empty();
        }

        return Optional.of(valueNode.get(1).asText());
    }
}
