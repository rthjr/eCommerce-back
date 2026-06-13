package com.ecommerce.correlation.model;

import java.util.List;

public record TraceSummary(
        String traceId,
        int spanCount,
        int errorSpanCount,
        long maxDurationMs,
        List<String> services,
        List<String> spanNames
) {
}
