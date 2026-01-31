package com.bookstore.gateway.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class GatewayIntegrationTest {

    private static final String CATALOG_BOOKS_PATH = "/api/v1/books";
    private static final String ORDERS_PATH = "/api/v1/orders";
    private static final String AUTH_PATH = "/api/v1/auth/login";
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
        stubFor(get(urlEqualTo(CATALOG_BOOKS_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_BOOK_RESPONSE)));

        webTestClient.get()
                .uri(CATALOG_BOOKS_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Test Book");
    }

    @Test
    void shouldRouteOrderServiceRequests() {
        stubFor(get(urlEqualTo(ORDERS_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_ORDER_RESPONSE)));

        webTestClient.get()
                .uri(ORDERS_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.status").isEqualTo("PENDING");
    }

    @Test
    void shouldRouteUserServiceRequests() {
        stubFor(post(urlEqualTo(AUTH_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_AUTH_RESPONSE)));

        webTestClient.post()
                .uri(AUTH_PATH)
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
        stubFor(get(urlEqualTo(CATALOG_BOOKS_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .withBody(TEST_BOOK_RESPONSE)));

        webTestClient.get()
                .uri(CATALOG_BOOKS_PATH)
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    @Test
    void shouldHandleOptionsRequestForCors() {
        webTestClient.options()
                .uri(CATALOG_BOOKS_PATH)
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
        stubFor(get(urlEqualTo(CATALOG_BOOKS_PATH))
                .willReturn(aResponse()
                        .withStatus(503)));

        webTestClient.get()
                .uri(CATALOG_BOOKS_PATH)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldHandleServiceTimeout() {
        stubFor(get(urlEqualTo(CATALOG_BOOKS_PATH))
                .willReturn(aResponse()
                        .withFixedDelay(30000)
                        .withStatus(200)));

        webTestClient.get()
                .uri(CATALOG_BOOKS_PATH)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
