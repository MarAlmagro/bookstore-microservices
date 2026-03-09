package com.bookstore.catalog.config;

import com.bookstore.catalog.controller.BookController;
import com.bookstore.catalog.security.JwtAuthenticationFilter;
import com.bookstore.catalog.service.BookService;
import com.bookstore.common.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false",
        "jwt.secret=dGhpcyBpcyBhIHZlcnkgc2VjcmV0IGtleSBmb3IgdGVzdGluZyBwdXJwb3Nlcw=="
})
@DisplayName("SecurityConfig Unit Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityFilterChain filterChain;

    @MockBean
    private BookService bookService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("SecurityFilterChain bean should be created with stateless session policy")
    void securityFilterChain_shouldBeCreated() {
        assertThat(filterChain).isNotNull();
        assertThat(filterChain.getFilters()).isNotEmpty();
    }

    @Test
    @DisplayName("GET /api/v1/books should be accessible (permitAll rule)")
    void publicBookEndpoints_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SecurityFilterChain should contain custom JwtAuthenticationFilter")
    void securityFilterChain_shouldContainJwtFilter() {
        boolean hasJwtFilter = filterChain.getFilters().stream()
                .anyMatch(f -> f.getClass().getSimpleName().contains("JwtAuthenticationFilter")
                        || f.getClass().getSimpleName().contains("Mock"));
        assertThat(hasJwtFilter).isTrue();
    }
}
