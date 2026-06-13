package com.ecommerce.correlation.model;

import java.time.Instant;
import java.util.Map;

public record LokiLogEntry(
        Instant timestamp,
        String message,
        Map<String, String> labels,
        String traceId
) {
}
