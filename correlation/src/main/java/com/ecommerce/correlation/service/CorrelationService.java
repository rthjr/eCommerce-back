package com.ecommerce.correlation.service;

import com.ecommerce.correlation.client.LokiClient;
import com.ecommerce.correlation.client.PrometheusClient;
import com.ecommerce.correlation.client.ZipkinClient;
import com.ecommerce.correlation.config.CorrelationProperties;
import com.ecommerce.correlation.exception.BackendQueryException;
import com.ecommerce.correlation.exception.CorrelationNotFoundException;
import com.ecommerce.correlation.model.AlertIngestionResponse;
import com.ecommerce.correlation.model.AlertmanagerWebhookRequest;
import com.ecommerce.correlation.model.CorrelationReport;
import com.ecommerce.correlation.model.LokiLogEntry;
import com.ecommerce.correlation.model.RootCauseSuspect;
import com.ecommerce.correlation.model.TraceSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CorrelationService {
    private final LokiClient lokiClient;
    private final ZipkinClient zipkinClient;
    private final PrometheusClient prometheusClient;
    private final TraceIdExtractor traceIdExtractor;
    private final RootCauseAnalyzer rootCauseAnalyzer;
    private final CorrelationProperties properties;
    private final Map<String, CachedCorrelation> correlationCache = new ConcurrentHashMap<>();

    public CorrelationService(
            LokiClient lokiClient,
            ZipkinClient zipkinClient,
            PrometheusClient prometheusClient,
            TraceIdExtractor traceIdExtractor,
            RootCauseAnalyzer rootCauseAnalyzer,
            CorrelationProperties properties
    ) {
        this.lokiClient = lokiClient;
        this.zipkinClient = zipkinClient;
        this.prometheusClient = prometheusClient;
        this.traceIdExtractor = traceIdExtractor;
        this.rootCauseAnalyzer = rootCauseAnalyzer;
        this.properties = properties;
    }

    public AlertIngestionResponse processAlert(AlertmanagerWebhookRequest payload) {
        if (payload.getAlerts() == null || payload.getAlerts().isEmpty()) {
            throw new IllegalArgumentException("Alert payload must include at least one alert.");
        }

        Instant now = Instant.now();
        var fingerprints = new ArrayList<String>();
        var reports = new ArrayList<CorrelationReport>();

        for (AlertmanagerWebhookRequest.Alert alert : payload.getAlerts()) {
            CorrelationReport report = correlateAlert(alert, payload.getCommonLabels(), payload.getCommonAnnotations(), now);
            correlationCache.put(report.alertFingerprint(), new CachedCorrelation(report, now.plus(properties.getCacheTtl())));
            fingerprints.add(report.alertFingerprint());
            reports.add(report);
        }

        evictExpired(now);
        return new AlertIngestionResponse(now, fingerprints.size(), List.copyOf(fingerprints), List.copyOf(reports));
    }

    public CorrelationReport getCorrelation(String fingerprint) {
        CachedCorrelation cached = correlationCache.get(fingerprint);
        if (cached == null || cached.expiresAt().isBefore(Instant.now())) {
            correlationCache.remove(fingerprint);
            throw new CorrelationNotFoundException("No correlation report found for fingerprint: " + fingerprint);
        }
        return cached.report();
    }

    public Map<String, Object> health() {
        evictExpired(Instant.now());
        return Map.of(
                "status", "UP",
                "cachedReports", correlationCache.size(),
                "lookbackMinutes", properties.getLookback().toMinutes()
        );
    }

    private CorrelationReport correlateAlert(
            AlertmanagerWebhookRequest.Alert alert,
            Map<String, String> commonLabels,
            Map<String, String> commonAnnotations,
            Instant now
    ) {
        Map<String, String> labels = mergeMap(commonLabels, alert.getLabels());
        Map<String, String> annotations = mergeMap(commonAnnotations, alert.getAnnotations());

        String service = firstNonBlank(labels.get("service"), labels.get("application"), "unknown-service");
        String pod = labels.getOrDefault("pod", "");
        String instance = labels.getOrDefault("instance", "");
        String fingerprint = buildFingerprint(alert, labels);
        String alertName = firstNonBlank(labels.get("alertname"), "unknown-alert");
        String summary = firstNonBlank(annotations.get("summary"), annotations.get("description"), "No summary provided");

        Instant windowEnd = now;
        Instant windowStart = resolveWindowStart(alert.getStartsAt(), now);
        List<String> warnings = new ArrayList<>();

        List<LokiLogEntry> logs = List.of();
        try {
            logs = lokiClient.queryLogs(labels, windowStart, windowEnd, properties.getMaxLogLines(), properties.getLogLevelFilter());
        } catch (BackendQueryException ex) {
            warnings.add("Loki query failed: " + ex.getMessage());
            log.warn("Loki correlation query failed for fingerprint={}: {}", fingerprint, ex.getMessage());
        }

        Set<String> traceIds = traceIdExtractor.extractTraceIds(logs, properties.getMaxTraceIds());
        String traceFromLabel = firstNonBlank(labels.get("trace_id"), labels.get("traceId"), "");
        if (StringUtils.hasText(traceFromLabel) && traceIds.size() < properties.getMaxTraceIds()) {
            traceIds.add(traceFromLabel.toLowerCase());
        }

        List<TraceSummary> traces = new ArrayList<>();
        for (String traceId : traceIds) {
            try {
                zipkinClient.fetchTrace(traceId).ifPresent(traces::add);
            } catch (BackendQueryException ex) {
                warnings.add("Zipkin query failed for trace %s: %s".formatted(traceId, ex.getMessage()));
                log.warn("Zipkin correlation query failed for traceId={}: {}", traceId, ex.getMessage());
            }
        }

        String metricQuery = prometheusClient.getRenderedQuery(service);
        String metricValue = annotations.get("metric_value");
        if (!StringUtils.hasText(metricValue)) {
            try {
                metricValue = prometheusClient.queryMetricValue(service).orElse("n/a");
            } catch (BackendQueryException ex) {
                metricValue = "n/a";
                warnings.add("Prometheus query failed: " + ex.getMessage());
                log.warn("Prometheus correlation query failed for service={}: {}", service, ex.getMessage());
            }
        }

        List<RootCauseSuspect> suspects = rootCauseAnalyzer.analyze(logs, traces);
        Map<String, String> links = buildDashboardLinks(service, pod, instance, windowStart, windowEnd, metricQuery);

        return new CorrelationReport(
                fingerprint,
                alertName,
                firstNonBlank(alert.getStatus(), "firing"),
                summary,
                service,
                instance,
                pod,
                windowStart,
                windowEnd,
                metricValue,
                metricQuery,
                List.copyOf(logs),
                List.copyOf(traces),
                List.copyOf(suspects),
                links,
                List.copyOf(warnings),
                now
        );
    }

    private Instant resolveWindowStart(String startsAt, Instant now) {
        Instant fallback = now.minus(properties.getLookback());
        if (!StringUtils.hasText(startsAt)) {
            return fallback;
        }

        try {
            Instant parsed = Instant.parse(startsAt);
            return parsed.isAfter(fallback) ? parsed : fallback;
        } catch (DateTimeParseException ex) {
            return fallback;
        }
    }

    private String buildFingerprint(AlertmanagerWebhookRequest.Alert alert, Map<String, String> labels) {
        if (StringUtils.hasText(alert.getFingerprint())) {
            return alert.getFingerprint();
        }

        String raw = labels + "|" + alert.getStartsAt() + "|" + alert.getStatus();
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, String> buildDashboardLinks(
            String service,
            String pod,
            String instance,
            Instant start,
            Instant end,
            String metricQuery
    ) {
        String encodedService = urlEncode(service);
        long from = start.toEpochMilli();
        long to = end.toEpochMilli();

        String lokiSelector = "{service=\"" + service.replace("\"", "\\\"") + "\"}";
        if (StringUtils.hasText(pod)) {
            lokiSelector = "{service=\"" + service.replace("\"", "\\\"") + "\",pod=\"" + pod.replace("\"", "\\\"") + "\"}";
        } else if (StringUtils.hasText(instance)) {
            lokiSelector = "{service=\"" + service.replace("\"", "\\\"") + "\",instance=\"" + instance.replace("\"", "\\\"") + "\"}";
        }

        Map<String, String> links = new LinkedHashMap<>();
        links.put("grafana", properties.getGrafanaDashboardUrl() + "?var-service=" + encodedService + "&from=" + from + "&to=" + to);
        links.put("prometheus", properties.getPrometheus().getBaseUrl() + "/graph?g0.expr=" + urlEncode(metricQuery));
        links.put("loki", properties.getLoki().getBaseUrl() + "/loki/api/v1/query_range?query=" + urlEncode(lokiSelector + " |= \"" + properties.getLogLevelFilter() + "\""));
        links.put("zipkin", properties.getZipkin().getBaseUrl() + "/zipkin/?serviceName=" + encodedService + "&lookback=custom&endTs=" + to + "&limit=20");
        return links;
    }

    private Map<String, String> mergeMap(Map<String, String> left, Map<String, String> right) {
        var result = new HashMap<String, String>();
        if (left != null) {
            result.putAll(left);
        }
        if (right != null) {
            result.putAll(right);
        }
        return result;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void evictExpired(Instant now) {
        correlationCache.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record CachedCorrelation(CorrelationReport report, Instant expiresAt) {
    }
}
