package com.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

//@Component
public class LoggingFilter implements GlobalFilter{
	
	
	private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoggingFilter.class);
	
	@Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Incoming request to: {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }
}
