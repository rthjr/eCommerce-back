package com.ecommerce.order.clients;

import com.ecommerce.order.dtos.PaymentGatewayRefundRequest;
import com.ecommerce.order.dtos.PaymentGatewayRefundResponse;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class PaymentGatewayRefundClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public PaymentGatewayRefundClient(
            @Value("${payment.gateway.base-url:http://localhost:8976}") String paymentGatewayBaseUrl,
            @Value("${payment.gateway.internal-api-key:}") String internalApiKey,
            ObjectProvider<ObservationRegistry> observationRegistryProvider,
            ObjectProvider<Tracer> tracerProvider,
            ObjectProvider<Propagator> propagatorProvider
    ) {
        ObservationRegistry observationRegistry = observationRegistryProvider.getIfAvailable();
        Tracer tracer = tracerProvider.getIfAvailable();
        Propagator propagator = propagatorProvider.getIfAvailable();

        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(paymentGatewayBaseUrl);
        if (observationRegistry != null && tracer != null && propagator != null) {
            restClientBuilder.requestInterceptor((request, body, execution) -> {
                if (tracer.currentSpan() != null) {
                    propagator.inject(
                            tracer.currentTraceContext().context(),
                            request.getHeaders(),
                            (carrier, key, value) -> carrier.add(key, value)
                    );
                }
                return execution.execute(request, body);
            });
        }

        this.restClient = restClientBuilder.build();
        this.internalApiKey = internalApiKey;
    }

    public PaymentGatewayRefundResponse refundQrPayment(PaymentGatewayRefundRequest request) {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Payment gateway internal API key is not configured"
            );
        }

        try {
            PaymentGatewayRefundResponse response = restClient.post()
                    .uri("/refunds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Internal-Api-Key", internalApiKey)
                    .body(request)
                    .retrieve()
                    .body(PaymentGatewayRefundResponse.class);

            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Payment gateway returned an empty refund response"
                );
            }

            return response;
        } catch (RestClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            log.error("Payment gateway refund call failed: status={} body={}", ex.getStatusCode(), body);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment gateway refund failed: " + ex.getStatusCode()
            );
        } catch (ResourceAccessException ex) {
            log.error("Payment gateway is unreachable: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment gateway is unreachable");
        }
    }
}
