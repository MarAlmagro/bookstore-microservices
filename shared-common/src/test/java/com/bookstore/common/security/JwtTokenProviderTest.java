package com.bookstore.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "testSecretKeyThatIsLongEnoughForHS256AlgorithmToWorkProperly";
    private static final String TEST_USERNAME = "test@example.com";
    private static final Long TEST_USER_ID = 123L;
    private static final String TEST_ROLE = "CUSTOMER";
    private static final long JWT_EXPIRATION_MS = 3600000L;
    private static final long JWT_REFRESH_EXPIRATION_MS = 7200000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", JWT_EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", JWT_REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken should create valid JWT token from Authentication")
    void generateToken_withAuthentication_shouldCreateValidToken() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("generateTokenFromUsername should create valid JWT token")
    void generateTokenFromUsername_withValidUsername_shouldCreateValidToken() {
        String token = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("generateTokenWithClaims should create token with custom claims")
    void generateTokenWithClaims_withCustomClaims_shouldIncludeClaims() {
        String token = jwtTokenProvider.generateTokenWithClaims(TEST_USERNAME, TEST_USER_ID, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(TEST_USERNAME);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(TEST_USER_ID);
        
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertThat(roles).isNotNull();
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0)).isEqualTo(TEST_ROLE);
    }

    @Test
    @DisplayName("generateTokenWithClaims should create token without userId when null")
    void generateTokenWithClaims_withNullUserId_shouldCreateTokenWithoutUserId() {
        String token = jwtTokenProvider.generateTokenWithClaims(TEST_USERNAME, null, TEST_ROLE);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(TEST_USERNAME);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isNull();
    }

    @Test
    @DisplayName("generateTokenWithClaims should create token without role when null")
    void generateTokenWithClaims_withNullRole_shouldCreateTokenWithoutRole() {
        String token = jwtTokenProvider.generateTokenWithClaims(TEST_USERNAME, TEST_USER_ID, null);

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(TEST_USERNAME);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(TEST_USER_ID);
        assertThat(jwtTokenProvider.getRolesFromToken(token)).isNull();
    }

    @Test
    @DisplayName("generateRefreshToken should create valid refresh token")
    void generateRefreshToken_withValidUsername_shouldCreateValidToken() {
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshToken)).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("validateToken should return true for valid token")
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken should return false for expired token")
    void validateToken_withExpiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        String expiredToken = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", JWT_EXPIRATION_MS);
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken should return false for malformed token")
    void validateToken_withMalformedToken_shouldReturnFalse() {
        String malformedToken = "this.is.not.a.valid.jwt.token";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken should return false for empty token")
    void validateToken_withEmptyToken_shouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("getUsernameFromToken should extract username correctly")
    void getUsernameFromToken_withValidToken_shouldExtractUsername() {
        String token = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("getUserIdFromToken should extract userId correctly")
    void getUserIdFromToken_withValidToken_shouldExtractUserId() {
        String token = jwtTokenProvider.generateTokenWithClaims(TEST_USERNAME, TEST_USER_ID, TEST_ROLE);

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("getRolesFromToken should extract roles correctly")
    void getRolesFromToken_withValidToken_shouldExtractRoles() {
        String token = jwtTokenProvider.generateTokenWithClaims(TEST_USERNAME, TEST_USER_ID, TEST_ROLE);

        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        assertThat(roles).isNotNull();
        assertThat(roles).hasSize(1);
        assertThat(roles).contains(TEST_ROLE);
    }

    @Test
    @DisplayName("token should have correct expiration time")
    void generateToken_shouldHaveCorrectExpirationTime() {
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);
        long afterGeneration = System.currentTimeMillis();

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date expiration = claims.getExpiration();
        long expectedMinExpiration = (beforeGeneration / 1000) * 1000 + JWT_EXPIRATION_MS;
        long expectedMaxExpiration = ((afterGeneration / 1000) + 1) * 1000 + JWT_EXPIRATION_MS;

        assertThat(expiration.getTime()).isBetween(expectedMinExpiration, expectedMaxExpiration);
    }

    @Test
    @DisplayName("refresh token should have longer expiration than regular token")
    void generateRefreshToken_shouldHaveLongerExpiration() {
        String regularToken = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);
        String refreshToken = jwtTokenProvider.generateRefreshToken(TEST_USERNAME);

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        
        Claims regularClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(regularToken)
                .getBody();
        
        Claims refreshClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        assertThat(refreshClaims.getExpiration()).isAfter(regularClaims.getExpiration());
    }

    @Test
    @DisplayName("token should have issued at timestamp")
    void generateToken_shouldHaveIssuedAtTimestamp() {
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtTokenProvider.generateTokenFromUsername(TEST_USERNAME);
        long afterGeneration = System.currentTimeMillis();

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date issuedAt = claims.getIssuedAt();
        long expectedMinIssuedAt = (beforeGeneration / 1000) * 1000;
        long expectedMaxIssuedAt = ((afterGeneration / 1000) + 1) * 1000;
        assertThat(issuedAt.getTime()).isBetween(expectedMinIssuedAt, expectedMaxIssuedAt);
    }

    @Test
    @DisplayName("generateTokenWithClaims should handle ADMIN role")
    void generateTokenWithClaims_withAdminRole_shouldIncludeAdminRole() {
        String token = jwtTokenProvider.generateTokenWithClaims(TEST_USERNAME, TEST_USER_ID, "ADMIN");

        List<String> roles = jwtTokenProvider.getRolesFromToken(token);
        assertThat(roles).contains("ADMIN");
    }
}
