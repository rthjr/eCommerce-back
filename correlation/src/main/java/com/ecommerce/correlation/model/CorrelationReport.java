package com.ecommerce.correlation.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CorrelationReport(
        String alertFingerprint,
        String alertName,
        String alertStatus,
        String summary,
        String service,
        String instance,
        String pod,
        Instant windowStart,
        Instant windowEnd,
        String metricValue,
        String metricQuery,
        List<LokiLogEntry> errorLogs,
        List<TraceSummary> relatedTraces,
        List<RootCauseSuspect> rootCauseSuspects,
        Map<String, String> dashboardLinks,
        List<String> warnings,
        Instant generatedAt
) {
}
