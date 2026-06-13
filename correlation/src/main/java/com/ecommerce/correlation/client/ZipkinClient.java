package com.ecommerce.correlation.client;

import com.ecommerce.correlation.exception.BackendQueryException;
import com.ecommerce.correlation.model.TraceSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ZipkinClient {
    private final RestClient zipkinRestClient;
    private final ObjectMapper objectMapper;

    public ZipkinClient(@Qualifier("zipkinRestClient") RestClient zipkinRestClient, ObjectMapper objectMapper) {
        this.zipkinRestClient = zipkinRestClient;
        this.objectMapper = objectMapper;
    }

    public Optional<TraceSummary> fetchTrace(String traceId) {
        try {
            String response = zipkinRestClient.get()
                    .uri("/api/v2/trace/{traceId}", traceId)
                    .retrieve()
                    .body(String.class);

            return summarizeTrace(traceId, response);
        } catch (RestClientResponseException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            if (statusCode.value() == 404) {
                return Optional.empty();
            }
            throw new BackendQueryException("zipkin", "Zipkin returned non-success response: " + statusCode.value(), ex);
        } catch (RestClientException ex) {
            throw new BackendQueryException("zipkin", "Failed to query Zipkin traces", ex);
        } catch (JsonProcessingException ex) {
            throw new BackendQueryException("zipkin", "Failed to parse Zipkin trace payload", ex);
        }
    }

    private Optional<TraceSummary> summarizeTrace(String traceId, String payload) throws JsonProcessingException {
        if (payload == null || payload.isBlank()) {
            return Optional.empty();
        }

        List<Map<String, Object>> spans = objectMapper.readValue(payload, new TypeReference<>() {
        });
        if (spans.isEmpty()) {
            return Optional.empty();
        }

        int errorSpanCount = 0;
        long maxDurationMs = 0;
        Set<String> services = new LinkedHashSet<>();
        List<String> spanNames = new ArrayList<>();

        for (Map<String, Object> span : spans) {
            if (isErrorSpan(span)) {
                errorSpanCount++;
            }

            Number durationMicros = asNumber(span.get("duration"));
            if (durationMicros != null) {
                maxDurationMs = Math.max(maxDurationMs, durationMicros.longValue() / 1_000);
            }

            Object localEndpoint = span.get("localEndpoint");
            if (localEndpoint instanceof Map<?, ?> endpointMap) {
                Object serviceName = endpointMap.get("serviceName");
                if (serviceName instanceof String name && !name.isBlank()) {
                    services.add(name);
                }
            }

            Object spanName = span.get("name");
            if (spanName instanceof String name && !name.isBlank()) {
                spanNames.add(name);
            }
        }

        return Optional.of(new TraceSummary(
                traceId,
                spans.size(),
                errorSpanCount,
                maxDurationMs,
                new ArrayList<>(services),
                spanNames.stream().distinct().limit(10).toList()
        ));
    }

    private boolean isErrorSpan(Map<String, Object> span) {
        Object tags = span.get("tags");
        if (!(tags instanceof Map<?, ?> tagMap)) {
            return false;
        }

        Object errorTag = tagMap.get("error");
        if (errorTag != null && !"false".equalsIgnoreCase(errorTag.toString())) {
            return true;
        }

        Object statusCode = tagMap.get("http.status_code");
        Number statusNumber = asNumber(statusCode);
        return statusNumber != null && statusNumber.intValue() >= 500;
    }

    private Number asNumber(Object value) {
        if (value instanceof Number number) {
            return number;
        }

        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        return null;
    }
}
