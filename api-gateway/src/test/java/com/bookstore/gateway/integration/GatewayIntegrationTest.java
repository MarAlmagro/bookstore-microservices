package com.bookstore.gateway.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class GatewayIntegrationTest {

    @Value("${api.gateway.paths.books}")
    private String catalogBooksPath;

    @Value("${api.gateway.paths.orders}")
    private String ordersPath;

    @Value("${api.gateway.paths.auth.login}")
    private String authPath;
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String TEST_BOOK_RESPONSE = "{\"id\":1,\"title\":\"Test Book\"}";
    private static final String TEST_ORDER_RESPONSE = "{\"id\":\"123\",\"status\":\"PENDING\"}";
    private static final String TEST_AUTH_RESPONSE = "{\"token\":\"test-jwt-token\"}";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    void shouldRouteCatalogServiceRequests() {
        stubFor(get(urlEqualTo(catalogBooksPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_BOOK_RESPONSE)));

        webTestClient.get()
                .uri(catalogBooksPath)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Test Book");
    }

    @Test
    void shouldRouteOrderServiceRequests() {
        stubFor(get(urlEqualTo(ordersPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_ORDER_RESPONSE)));

        webTestClient.get()
                .uri(ordersPath)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void shouldRouteUserServiceRequests() {
        stubFor(post(urlEqualTo(authPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_AUTH_RESPONSE)));

        webTestClient.post()
                .uri(authPath)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"test\",\"password\":\"test\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.token").isEqualTo("test-jwt-token");
    }

    @Test
    void shouldIncludeCorsHeaders() {
        stubFor(get(urlEqualTo(catalogBooksPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_BOOK_RESPONSE)));

        webTestClient.get()
                .uri(catalogBooksPath)
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    @Test
    void shouldHandleOptionsRequestForCors() {
        webTestClient.options()
                .uri(catalogBooksPath)
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
    }

    @Test
    void shouldReturn404ForUnknownRoute() {
        webTestClient.get()
                .uri("/api/v1/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldHandleServiceUnavailable() {
        stubFor(get(urlEqualTo(catalogBooksPath))
                .willReturn(aResponse()
                        .withStatus(503)));

        webTestClient.get()
                .uri(catalogBooksPath)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldHandleServiceTimeout() {
        stubFor(get(urlEqualTo(catalogBooksPath))
                .willReturn(aResponse()
                        .withFixedDelay(3000)
                        .withStatus(200)));

        webTestClient.mutate()
                .responseTimeout(Duration.ofSeconds(5))
                .build()
                .get()
                .uri(catalogBooksPath)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
