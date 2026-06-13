package com.ecommerce.correlation.service;

import com.ecommerce.correlation.config.CorrelationProperties;
import com.ecommerce.correlation.model.LokiLogEntry;
import com.ecommerce.correlation.model.TraceSummary;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RootCauseAnalyzerTest {

    @Test
    void shouldPrioritizeDetectedLogPatterns() {
        CorrelationProperties properties = new CorrelationProperties();
        RootCauseAnalyzer analyzer = new RootCauseAnalyzer(properties);

        List<LokiLogEntry> logs = List.of(
                new LokiLogEntry(Instant.now(), "database timeout while fetching cart", Map.of(), ""),
                new LokiLogEntry(Instant.now(), "database timeout while fetching order", Map.of(), "")
        );
        List<TraceSummary> traces = List.of(
                new TraceSummary("trace-1", 8, 1, 250, List.of("order-service"), List.of("dbCall"))
        );

        var suspects = analyzer.analyze(logs, traces);
        assertThat(suspects).isNotEmpty();
        assertThat(suspects.getFirst().target()).isIn("timeout", "database", "order-service");
    }
}
