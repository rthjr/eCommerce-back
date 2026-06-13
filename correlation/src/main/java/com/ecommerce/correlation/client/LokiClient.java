package com.ecommerce.correlation.client;

import com.ecommerce.correlation.exception.BackendQueryException;
import com.ecommerce.correlation.model.LokiLogEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LokiClient {
    private final RestClient lokiRestClient;
    private final ObjectMapper objectMapper;

    public LokiClient(@Qualifier("lokiRestClient") RestClient lokiRestClient, ObjectMapper objectMapper) {
        this.lokiRestClient = lokiRestClient;
        this.objectMapper = objectMapper;
    }

    public List<LokiLogEntry> queryLogs(Map<String, String> labels, Instant start, Instant end, int limit, String levelFilter) {
        String selector = buildSelector(labels);
        String query = selector + " |= \"" + levelFilter + "\"";

        try {
            String response = lokiRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/loki/api/v1/query_range")
                            .queryParam("query", query)
                            .queryParam("start", toNanoEpoch(start))
                            .queryParam("end", toNanoEpoch(end))
                            .queryParam("limit", limit)
                            .queryParam("direction", "backward")
                            .build())
                    .retrieve()
                    .body(String.class);

            return parseQueryResponse(response);
        } catch (RestClientException ex) {
            throw new BackendQueryException("loki", "Failed to query Loki logs", ex);
        } catch (JsonProcessingException ex) {
            throw new BackendQueryException("loki", "Failed to parse Loki response", ex);
        }
    }

    private List<LokiLogEntry> parseQueryResponse(String response) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(response);
        JsonNode resultNode = root.path("data").path("result");
        var entries = new ArrayList<LokiLogEntry>();

        if (!resultNode.isArray()) {
            return entries;
        }

        for (JsonNode streamNode : resultNode) {
            Map<String, String> streamLabels = objectMapper.convertValue(
                    streamNode.path("stream"),
                    new TypeReference<HashMap<String, String>>() {
                    }
            );

            JsonNode valuesNode = streamNode.path("values");
            if (!valuesNode.isArray()) {
                continue;
            }

            for (JsonNode valueNode : valuesNode) {
                if (!valueNode.isArray() || valueNode.size() < 2) {
                    continue;
                }

                String nanos = valueNode.get(0).asText();
                String message = valueNode.get(1).asText();
                Instant timestamp;
                try {
                    timestamp = parseNanos(nanos);
                } catch (NumberFormatException | ArithmeticException ex) {
                    continue;
                }

                entries.add(new LokiLogEntry(
                        timestamp,
                        message,
                        streamLabels,
                        streamLabels.getOrDefault("trace_id", "")
                ));
            }
        }

        return entries;
    }

    private String buildSelector(Map<String, String> labels) {
        var matchers = new ArrayList<String>();
        addMatcher(matchers, "service", labels.get("service"));
        addMatcher(matchers, "application", labels.get("application"));
        addMatcher(matchers, "pod", labels.get("pod"));
        addMatcher(matchers, "instance", labels.get("instance"));
        addMatcher(matchers, "namespace", labels.get("namespace"));

        if (matchers.isEmpty()) {
            return "{job=~\".+\"}";
        }

        return "{" + String.join(",", matchers) + "}";
    }

    private void addMatcher(List<String> matchers, String key, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        matchers.add(key + "=\"" + value.replace("\"", "\\\"") + "\"");
    }

    private String toNanoEpoch(Instant instant) {
        BigInteger seconds = BigInteger.valueOf(instant.getEpochSecond()).multiply(BigInteger.valueOf(1_000_000_000L));
        BigInteger nanos = seconds.add(BigInteger.valueOf(instant.getNano()));
        return nanos.toString();
    }

    private Instant parseNanos(String nanosTimestamp) {
        BigInteger nanos = new BigInteger(nanosTimestamp);
        BigInteger[] split = nanos.divideAndRemainder(BigInteger.valueOf(1_000_000_000L));
        return Instant.ofEpochSecond(split[0].longValueExact(), split[1].longValueExact());
    }
}
