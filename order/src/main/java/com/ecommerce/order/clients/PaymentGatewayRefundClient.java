package com.ecommerce.order.clients;

import com.ecommerce.order.dtos.PaymentGatewayRefundRequest;
import com.ecommerce.order.dtos.PaymentGatewayRefundResponse;
import lombok.extern.slf4j.Slf4j;
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
            @Value("${payment.gateway.internal-api-key:}") String internalApiKey
    ) {
        this.restClient = RestClient.builder().baseUrl(paymentGatewayBaseUrl).build();
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
