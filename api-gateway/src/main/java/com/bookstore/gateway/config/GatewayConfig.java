package com.bookstore.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class GatewayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);
    
    @Bean
    @Order(1)
    public GlobalFilter requestLoggingFilter() {
        return (exchange, chain) -> {
            logger.info("Request: {} {}", 
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI());
            return chain.filter(exchange);
        };
    }
}
