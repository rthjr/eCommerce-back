package com.ecom.payment.client;

import com.ecom.payment.dto.CreateOrderRequest;
import com.ecom.payment.dto.OrderResponse;
import com.ecom.payment.dto.OrderStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PaymentGatewayClient {

    private final RestTemplate restTemplate;

    @Value("${payment.gateway.url:http://0.0.0.0:8000}")
    private String gatewayUrl;

    public OrderResponse createOrder(CreateOrderRequest request) {
        return restTemplate.postForObject(
            gatewayUrl + "/orders",
            request,
            OrderResponse.class
        );
    }

    public OrderStatusResponse markOrderPaid(String orderId) {
        return restTemplate.postForObject(
            gatewayUrl + "/orders/" + orderId + "/test-paid",
            null,
            OrderStatusResponse.class
        );
    }

    public OrderStatusResponse getOrderStatus(String orderId) {
        return restTemplate.getForObject(
            gatewayUrl + "/orders/" + orderId + "/status",
            OrderStatusResponse.class
        );
    }
}
