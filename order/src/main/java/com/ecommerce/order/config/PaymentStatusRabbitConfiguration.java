package com.ecommerce.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentStatusRabbitConfiguration {
    @Value("${payment.status.queue}")
    private String paymentStatusQueue;

    @Value("${payment.status.exchange}")
    private String paymentStatusExchange;

    @Value("${payment.status.routing-key}")
    private String paymentStatusRoutingKey;

    @Bean
    public Queue paymentStatusQueue() {
        return QueueBuilder.durable(paymentStatusQueue).build();
    }

    @Bean
    public TopicExchange paymentStatusExchange() {
        return ExchangeBuilder.topicExchange(paymentStatusExchange)
                .durable(true)
                .build();
    }

    @Bean
    public Binding paymentStatusBinding() {
        return BindingBuilder.bind(paymentStatusQueue())
                .to(paymentStatusExchange())
                .with(paymentStatusRoutingKey);
    }
}
