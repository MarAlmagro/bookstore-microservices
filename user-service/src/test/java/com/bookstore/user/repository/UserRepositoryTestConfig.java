package com.bookstore.user.repository;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal configuration for repository tests.
 * Excludes security and only loads JPA-related components.
 */
@Configuration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@EnableJpaRepositories(basePackages = "com.bookstore.user.repository")
@EntityScan(basePackages = "com.bookstore.user.entity")
public class UserRepositoryTestConfig {
}
