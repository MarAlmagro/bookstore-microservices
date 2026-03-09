package com.bookstore.catalog.security;

import com.bookstore.common.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TEST_USER = "testuser";

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("should set authentication when valid JWT is present")
    void doFilterInternal_withValidJwt_shouldSetAuthentication() throws Exception {
        String token = "valid-jwt-token";
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn(TEST_USER);
        when(tokenProvider.getRolesFromToken(token)).thenReturn(Arrays.asList("USER"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(TEST_USER);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("should set multiple roles when JWT contains multiple roles")
    void doFilterInternal_withMultipleRoles_shouldSetAllAuthorities() throws Exception {
        String token = "multi-role-token";
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn("admin");
        when(tokenProvider.getRolesFromToken(token)).thenReturn(Arrays.asList("USER", "ADMIN"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .hasSize(2)
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("should not set authentication when no Authorization header")
    void doFilterInternal_withNoAuthHeader_shouldNotSetAuthentication() throws Exception {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("should not set authentication when Authorization header has no Bearer prefix")
    void doFilterInternal_withNoBearerPrefix_shouldNotSetAuthentication() throws Exception {
        request.addHeader(AUTH_HEADER, "Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
    }

    @Test
    @DisplayName("should not set authentication when JWT is invalid")
    void doFilterInternal_withInvalidJwt_shouldNotSetAuthentication() throws Exception {
        String token = "invalid-jwt-token";
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should continue filter chain even when exception occurs")
    void doFilterInternal_whenExceptionOccurs_shouldContinueFilterChain() throws Exception {
        String token = "error-token";
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should handle empty Bearer token")
    void doFilterInternal_withEmptyBearerToken_shouldNotSetAuthentication() throws Exception {
        request.addHeader(AUTH_HEADER, BEARER_PREFIX);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should handle JWT with empty roles list")
    void doFilterInternal_withEmptyRoles_shouldSetAuthenticationWithNoAuthorities() throws Exception {
        String token = "no-roles-token";
        request.addHeader(AUTH_HEADER, BEARER_PREFIX + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken(token)).thenReturn(TEST_USER);
        when(tokenProvider.getRolesFromToken(token)).thenReturn(Collections.emptyList());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
    }
}
