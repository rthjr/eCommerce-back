package com.ecommerce.correlation.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "correlation")
public class CorrelationProperties {
    @NotNull
    private Duration lookback = Duration.ofMinutes(10);

    @Min(1)
    private int maxLogLines = 250;

    @Min(1)
    private int maxTraceIds = 20;

    @NotNull
    private Duration cacheTtl = Duration.ofMinutes(5);

    @NotNull
    private Duration backendTimeout = Duration.ofSeconds(2);

    @NotBlank
    private String grafanaDashboardUrl = "http://localhost:3000/d/correlation";

    @NotBlank
    private String logLevelFilter = "ERROR";

    private List<String> rootCauseKeywords = new ArrayList<>(List.of(
            "timeout", "exception", "refused", "database", "deadlock", "unavailable"
    ));

    private final Prometheus prometheus = new Prometheus();
    private final Loki loki = new Loki();
    private final Zipkin zipkin = new Zipkin();

    @Getter
    @Setter
    public static class Prometheus {
        @NotBlank
        private String baseUrl = "http://localhost:9090";

        @NotBlank
        private String metricQueryTemplate =
                "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{service=\"%s\"}[5m])) by (le))";
    }

    @Getter
    @Setter
    public static class Loki {
        @NotBlank
        private String baseUrl = "http://localhost:3100";
    }

    @Getter
    @Setter
    public static class Zipkin {
        @NotBlank
        private String baseUrl = "http://localhost:9411";
    }
}
