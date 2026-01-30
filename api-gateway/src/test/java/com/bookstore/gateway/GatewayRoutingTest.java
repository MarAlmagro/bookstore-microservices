package com.bookstore.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DisplayName("Gateway Routing Tests")
class GatewayRoutingTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String BOOKS_PATH = "/api/v1/books";
    private static final String BOOKS_PATH_PATTERN = "/api/v1/books.*";
    private static final String ORDERS_PATH = "/api/v1/orders";
    private static final String ORDERS_PATH_PATTERN = "/api/v1/orders.*";
    private static final String CART_PATH = "/api/v1/cart";
    private static final String AUTH_LOGIN_PATH = "/api/v1/auth/login";
    private static final String USERS_PATH = "/api/v1/users";
    private static final String USERS_PATH_PATTERN = "/api/v1/users.*";

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        resetAllRequests();
    }

    @Test
    @DisplayName("Should route GET request to catalog-service")
    void routeToCatalogService_whenGetBooks_shouldSucceed() {
        stubFor(get(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("[]")));

        webTestClient.get()
                .uri(BOOKS_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should route POST request to catalog-service")
    void routeToCatalogService_whenCreateBook_shouldSucceed() {
        String bookJson = "{" +
                "\"title\": \"Test Book\"," +
                "\"author\": \"Test Author\"," +
                "\"isbn\": \"1234567890\"," +
                "\"price\": 29.99," +
                "\"stock\": 10" +
                "}";

        stubFor(post(urlEqualTo(BOOKS_PATH))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(bookJson)));

        webTestClient.post()
                .uri(BOOKS_PATH)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .bodyValue(bookJson)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("Should route GET request to order-service for orders")
    void routeToOrderService_whenGetOrders_shouldSucceed() {
        stubFor(get(urlPathMatching(ORDERS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("[]")));

        webTestClient.get()
                .uri(ORDERS_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should route POST request to order-service for cart")
    void routeToOrderService_whenAddToCart_shouldSucceed() {
        String cartItemJson = "{" +
                "\"bookId\": \"123\"," +
                "\"quantity\": 2" +
                "}";

        stubFor(post(urlEqualTo(CART_PATH))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(cartItemJson)));

        webTestClient.post()
                .uri(CART_PATH)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .bodyValue(cartItemJson)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("Should route POST request to user-service for authentication")
    void routeToUserService_whenLogin_shouldSucceed() {
        String authJson = "{" +
                "\"email\": \"test@example.com\"," +
                "\"password\": \"password123\"" +
                "}";

        String tokenResponse = "{" +
                "\"token\": \"jwt-token-here\"," +
                "\"refreshToken\": \"refresh-token-here\"" +
                "}";

        stubFor(post(urlEqualTo(AUTH_LOGIN_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(tokenResponse)));

        webTestClient.post()
                .uri(AUTH_LOGIN_PATH)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .bodyValue(authJson)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should route GET request to user-service for users")
    void routeToUserService_whenGetUsers_shouldSucceed() {
        stubFor(get(urlPathMatching(USERS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("[]")));

        webTestClient.get()
                .uri(USERS_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON);
    }

    @Test
    @DisplayName("Should handle CORS preflight request")
    void handleCorsPreflightRequest_shouldSucceed() {
        webTestClient.options()
                .uri(BOOKS_PATH)
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectHeader().exists("Access-Control-Allow-Methods")
                .expectHeader().exists("Access-Control-Allow-Headers");
    }

    @Test
    @DisplayName("Should allow GET request with CORS headers")
    void handleCorsGetRequest_shouldIncludeCorsHeaders() {
        stubFor(get(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("[]")));

        webTestClient.get()
                .uri(BOOKS_PATH)
                .header("Origin", "http://localhost:3000")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin");
    }

    @Test
    @DisplayName("Should handle 404 when route not found")
    void handleNotFoundRoute_shouldReturn404() {
        webTestClient.get()
                .uri("/api/v1/nonexistent")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Should handle 503 when service is unavailable")
    void handleServiceUnavailable_shouldReturn503() {
        stubFor(get(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"error\":\"Service Unavailable\"}")));

        webTestClient.get()
                .uri(BOOKS_PATH)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Should forward custom headers to downstream service")
    void forwardCustomHeaders_shouldSucceed() {
        stubFor(get(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("[]")));

        webTestClient.get()
                .uri(BOOKS_PATH)
                .header("X-User-Id", "user123")
                .header("Authorization", "Bearer token123")
                .exchange()
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching(BOOKS_PATH_PATTERN))
                .withHeader("X-User-Id", equalTo("user123"))
                .withHeader("Authorization", equalTo("Bearer token123")));
    }

    @Test
    @DisplayName("Should handle PUT request to catalog-service")
    void routeToCatalogService_whenUpdateBook_shouldSucceed() {
        String bookJson = "{" +
                "\"id\": \"1\"," +
                "\"title\": \"Updated Book\"," +
                "\"author\": \"Updated Author\"," +
                "\"isbn\": \"1234567890\"," +
                "\"price\": 39.99," +
                "\"stock\": 15" +
                "}";

        stubFor(put(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(bookJson)));

        webTestClient.put()
                .uri(BOOKS_PATH + "/1")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .bodyValue(bookJson)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should handle DELETE request to catalog-service")
    void routeToCatalogService_whenDeleteBook_shouldSucceed() {
        stubFor(delete(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(204)));

        webTestClient.delete()
                .uri(BOOKS_PATH + "/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Should handle PATCH request to order-service")
    void routeToOrderService_whenUpdateOrderStatus_shouldSucceed() {
        String statusJson = "{" +
                "\"status\": \"SHIPPED\"" +
                "}";

        stubFor(patch(urlPathMatching(ORDERS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(statusJson)));

        webTestClient.patch()
                .uri(ORDERS_PATH + "/1")
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .bodyValue(statusJson)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should handle query parameters in routing")
    void handleQueryParameters_shouldForwardToService() {
        stubFor(get(urlPathMatching(BOOKS_PATH_PATTERN))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("[]")));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BOOKS_PATH)
                        .queryParam("category", "fiction")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(getRequestedFor(urlPathMatching(BOOKS_PATH_PATTERN))
                .withQueryParam("category", equalTo("fiction"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10")));
    }
}
