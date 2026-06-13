package com.ecommerce.correlation.service;

import com.ecommerce.correlation.model.LokiLogEntry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TraceIdExtractor {
    private static final List<Pattern> TRACE_PATTERNS = List.of(
            Pattern.compile("\"trace_id\"\\s*[:=]\\s*\"?([0-9a-fA-F]{16,32})\"?"),
            Pattern.compile("\"traceId\"\\s*[:=]\\s*\"?([0-9a-fA-F]{16,32})\"?"),
            Pattern.compile("\\btrace[_-]?id\\b\\s*[=:]\\s*\"?([0-9a-fA-F]{16,32})\"?", Pattern.CASE_INSENSITIVE)
    );

    public Set<String> extractTraceIds(List<LokiLogEntry> logs, int maxTraceIds) {
        var traceIds = new LinkedHashSet<String>();

        for (LokiLogEntry log : logs) {
            if (traceIds.size() >= maxTraceIds) {
                break;
            }

            if (StringUtils.hasText(log.traceId())) {
                traceIds.add(log.traceId().toLowerCase());
            }

            String message = log.message();
            if (!StringUtils.hasText(message)) {
                continue;
            }

            for (Pattern pattern : TRACE_PATTERNS) {
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    traceIds.add(matcher.group(1).toLowerCase());
                    if (traceIds.size() >= maxTraceIds) {
                        break;
                    }
                }
            }
        }

        return traceIds;
    }
}
