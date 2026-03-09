package com.bookstore.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimitConfig Unit Tests")
class RateLimitConfigTest {

    private static final String BOOKS_PATH = "/api/v1/books";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String TEST_IP = "192.168.1.100";

    private KeyResolver keyResolver;

    @BeforeEach
    void setUp() {
        keyResolver = new RateLimitConfig().userKeyResolver();
    }

    @Test
    @DisplayName("userKeyResolver bean should be created")
    void userKeyResolver_shouldBeCreated() {
        assertThat(keyResolver).isNotNull();
    }

    @Test
    @DisplayName("should resolve key from X-User-Id header when present")
    void resolve_withUserIdHeader_shouldReturnUserId() {
        MockServerHttpRequest request = MockServerHttpRequest.get(BOOKS_PATH)
                .header(USER_ID_HEADER, "user-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<String> result = keyResolver.resolve(exchange);

        StepVerifier.create(result)
                .expectNext("user-123")
                .verifyComplete();
    }

    @Test
    @DisplayName("should resolve key from remote address when X-User-Id header is absent")
    void resolve_withoutUserIdHeader_shouldReturnRemoteAddress() {
        InetSocketAddress remoteAddress = new InetSocketAddress(TEST_IP, 8080);
        MockServerHttpRequest request = MockServerHttpRequest.get(BOOKS_PATH)
                .remoteAddress(remoteAddress)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<String> result = keyResolver.resolve(exchange);

        StepVerifier.create(result)
                .expectNext(TEST_IP)
                .verifyComplete();
    }

    @Test
    @DisplayName("should resolve key from remote address when X-User-Id header is empty")
    void resolve_withEmptyUserIdHeader_shouldReturnRemoteAddress() {
        InetSocketAddress remoteAddress = new InetSocketAddress("10.0.0.1", 9090);
        MockServerHttpRequest request = MockServerHttpRequest.get(BOOKS_PATH)
                .header(USER_ID_HEADER, "")
                .remoteAddress(remoteAddress)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<String> result = keyResolver.resolve(exchange);

        StepVerifier.create(result)
                .expectNext("10.0.0.1")
                .verifyComplete();
    }

    @Test
    @DisplayName("should return 'unknown' when no X-User-Id and no remote address")
    void resolve_withNoUserIdAndNoRemoteAddress_shouldReturnUnknown() {
        MockServerHttpRequest request = MockServerHttpRequest.get(BOOKS_PATH)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<String> result = keyResolver.resolve(exchange);

        StepVerifier.create(result)
                .assertNext(key -> assertThat(key).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("should prefer X-User-Id header over remote address")
    void resolve_withBothUserIdAndRemoteAddress_shouldPreferUserId() {
        InetSocketAddress remoteAddress = new InetSocketAddress(TEST_IP, 8080);
        MockServerHttpRequest request = MockServerHttpRequest.get(BOOKS_PATH)
                .header(USER_ID_HEADER, "user-456")
                .remoteAddress(remoteAddress)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<String> result = keyResolver.resolve(exchange);

        StepVerifier.create(result)
                .expectNext("user-456")
                .verifyComplete();
    }
}
