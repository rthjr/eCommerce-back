package com.ecommerce.correlation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertmanagerWebhookRequest {
    private String receiver;
    private String status;
    private List<Alert> alerts = new ArrayList<>();
    private Map<String, String> commonLabels = new HashMap<>();
    private Map<String, String> commonAnnotations = new HashMap<>();
    private String externalURL;
    private String version;
    private String groupKey;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alert {
        private String status;
        private Map<String, String> labels = new HashMap<>();
        private Map<String, String> annotations = new HashMap<>();
        private String startsAt;
        private String endsAt;
        private String generatorURL;
        private String fingerprint;
    }
}
