package com.bookstore.catalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for Catalog Service.
 * CORS is centralized at API Gateway level.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

}
