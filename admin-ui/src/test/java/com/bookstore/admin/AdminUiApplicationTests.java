package com.bookstore.admin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class AdminUiApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring application context loads successfully with all configurations
        // This is a smoke test to ensure the application can start without errors
        // and all beans are properly configured in the test environment
        // with Eureka client and discovery disabled
    }
}
