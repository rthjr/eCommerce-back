package com.ecom.payment.messaging;

import com.ecom.payment.event.PaymentStatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentStatusPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${payment.status.exchange}")
    private String paymentStatusExchange;

    @Value("${payment.status.routing-key}")
    private String paymentStatusRoutingKey;

    public void publishPaid(PaymentStatusMessage message) {
        rabbitTemplate.convertAndSend(paymentStatusExchange, paymentStatusRoutingKey, message);
    }
}
