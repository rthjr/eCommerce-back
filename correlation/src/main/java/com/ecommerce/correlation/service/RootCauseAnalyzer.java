package com.ecommerce.correlation.service;

import com.ecommerce.correlation.config.CorrelationProperties;
import com.ecommerce.correlation.model.LokiLogEntry;
import com.ecommerce.correlation.model.RootCauseSuspect;
import com.ecommerce.correlation.model.TraceSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class RootCauseAnalyzer {
    private final CorrelationProperties properties;

    public RootCauseAnalyzer(CorrelationProperties properties) {
        this.properties = properties;
    }

    public List<RootCauseSuspect> analyze(List<LokiLogEntry> logs, List<TraceSummary> traces) {
        var suspects = new ArrayList<RootCauseSuspect>();
        var keywordHits = new HashMap<String, Integer>();

        for (String keyword : properties.getRootCauseKeywords()) {
            keywordHits.put(keyword.toLowerCase(Locale.ROOT), 0);
        }

        for (LokiLogEntry log : logs) {
            String message = log.message() == null ? "" : log.message().toLowerCase(Locale.ROOT);
            for (Map.Entry<String, Integer> entry : keywordHits.entrySet()) {
                if (message.contains(entry.getKey())) {
                    keywordHits.put(entry.getKey(), entry.getValue() + 1);
                }
            }
        }

        keywordHits.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .forEach(entry -> suspects.add(new RootCauseSuspect(
                        "log-pattern",
                        entry.getKey(),
                        "Found %d log lines containing '%s'".formatted(entry.getValue(), entry.getKey()),
                        entry.getValue() * 5
                )));

        for (TraceSummary trace : traces) {
            if (trace.errorSpanCount() <= 0) {
                continue;
            }

            String impactedService = trace.services().isEmpty() ? "unknown-service" : trace.services().getFirst();
            suspects.add(new RootCauseSuspect(
                    "trace-failure",
                    impactedService,
                    "Trace %s contains %d error spans".formatted(trace.traceId(), trace.errorSpanCount()),
                    Math.max(3, trace.errorSpanCount() * 3)
            ));
        }

        if (suspects.isEmpty()) {
            suspects.add(new RootCauseSuspect(
                    "insufficient-data",
                    "unknown",
                    "No clear root-cause signal found in current logs/traces window",
                    1
            ));
        }

        suspects.sort(Comparator.comparingInt(RootCauseSuspect::score).reversed());
        return suspects.stream().limit(8).toList();
    }
}
