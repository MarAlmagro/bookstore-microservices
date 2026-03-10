package com.bookstore.user.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(BLOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
        logger.debug("Login succeeded for key: {}, cache cleared", key);
    }

    public void loginFailed(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            logger.error("Error retrieving attempts for key: {}", key, e);
        }
        attempts++;
        attemptsCache.put(key, attempts);
        logger.warn("Login failed for key: {}, attempt count: {}", key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            int attempts = attemptsCache.get(key);
            boolean blocked = attempts >= MAX_ATTEMPTS;
            if (blocked) {
                logger.warn("Login blocked for key: {}, attempts: {}", key, attempts);
            }
            return blocked;
        } catch (ExecutionException e) {
            logger.error("Error checking if key is blocked: {}", key, e);
            return false;
        }
    }

    public int getAttempts(String key) {
        try {
            return attemptsCache.get(key);
        } catch (ExecutionException e) {
            logger.error("Error getting attempts for key: {}", key, e);
            return 0;
        }
    }
}
