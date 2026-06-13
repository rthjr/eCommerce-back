package com.ecommerce.correlation.model;

import java.time.Instant;
import java.util.List;

public record AlertIngestionResponse(
        Instant processedAt,
        int correlatedCount,
        List<String> fingerprints,
        List<CorrelationReport> reports
) {
}
