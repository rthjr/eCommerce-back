package com.ecommerce.correlation.service;

import com.ecommerce.correlation.model.LokiLogEntry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdExtractorTest {

    private final TraceIdExtractor traceIdExtractor = new TraceIdExtractor();

    @Test
    void shouldExtractTraceIdFromJsonAndKvMessages() {
        List<LokiLogEntry> logs = List.of(
                new LokiLogEntry(Instant.now(), "{\"trace_id\":\"abc123abc123abc1\",\"message\":\"boom\"}", java.util.Map.of(), ""),
                new LokiLogEntry(Instant.now(), "traceId=def456def456def4 timeout calling db", java.util.Map.of(), "")
        );

        var traceIds = traceIdExtractor.extractTraceIds(logs, 10);
        assertThat(traceIds).contains("abc123abc123abc1", "def456def456def4");
    }
}
