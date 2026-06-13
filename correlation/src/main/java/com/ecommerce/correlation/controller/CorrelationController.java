package com.ecommerce.correlation.controller;

import com.ecommerce.correlation.model.AlertIngestionResponse;
import com.ecommerce.correlation.model.AlertmanagerWebhookRequest;
import com.ecommerce.correlation.model.CorrelationReport;
import com.ecommerce.correlation.service.CorrelationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CorrelationController {
    private final CorrelationService correlationService;

    public CorrelationController(CorrelationService correlationService) {
        this.correlationService = correlationService;
    }

    @PostMapping("/alert")
    public ResponseEntity<AlertIngestionResponse> ingestAlert(@RequestBody AlertmanagerWebhookRequest payload) {
        return ResponseEntity.ok(correlationService.processAlert(payload));
    }

    @GetMapping("/correlation/{fingerprint}")
    public ResponseEntity<CorrelationReport> getCorrelation(@PathVariable String fingerprint) {
        return ResponseEntity.ok(correlationService.getCorrelation(fingerprint));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(correlationService.health());
    }
}
