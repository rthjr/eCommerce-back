package com.ecommerce.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	@Value("${rabbitmq.queue.name}")
	private String queueName;

	@Value("${rabbitmq.exchange.name}")
	private String exchangeName;

	@Value("${rabbitmq.routing.key}")
	private String routingKey;

	// Order queue (existing)
	@Bean
	public Queue orderQueue() {
		return QueueBuilder.durable(queueName).build();
	}

	@Bean
	public TopicExchange orderExchange() {
		return new TopicExchange(exchangeName, true, false);
	}

	@Bean
	public Binding orderBinding() {
		return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(routingKey);
	}

	// Payment queue (new)
	@Bean
	public Queue paymentQueue() {
		return QueueBuilder.durable("payment.events.queue").build();
	}

	@Bean
	public TopicExchange paymentExchange() {
		return new TopicExchange("payment.exchange", true, false);
	}

	@Bean
	public Binding paymentBinding() {
		return BindingBuilder.bind(paymentQueue()).to(paymentExchange()).with("payment.*");
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(messageConverter());
		factory.setObservationEnabled(true);
		return factory;
	}
}
