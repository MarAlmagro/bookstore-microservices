package com.bookstore.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

    private static final String TEST_EMAIL = "test@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String USER_ID_CLAIM = "userId";
    private static final String ROLES_CLAIM = "roles";
    private static final String JWT_EXPIRATION_FIELD = "jwtExpirationMs";

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret;
    private long jwtExpirationMs;
    private long jwtRefreshExpirationMs;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtSecret = "testSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256AlgorithmTestOnly";
        jwtExpirationMs = 86400000L;
        jwtRefreshExpirationMs = 604800000L;

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, JWT_EXPIRATION_FIELD, jwtExpirationMs);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpirationMs", jwtRefreshExpirationMs);
    }

    @Test
    @DisplayName("generateTokenWithClaims should create valid token with username only")
    void generateTokenWithClaims_withUsernameOnly_shouldCreateValidToken() {
        String username = TEST_EMAIL;

        String token = jwtTokenProvider.generateTokenWithClaims(username, null, null);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateTokenWithClaims should create token with all claims")
    void generateTokenWithClaims_withAllClaims_shouldIncludeAllClaims() {
        String username = TEST_EMAIL;
        Long userId = 123L;
        String role = "ADMIN";

        String token = jwtTokenProvider.generateTokenWithClaims(username, userId, role);

        assertThat(token).isNotNull();
        Claims claims = parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get(USER_ID_CLAIM, Long.class)).isEqualTo(userId);
        assertThat(claims.get(ROLES_CLAIM, List.class)).containsExactly(role);
    }

    @Test
    @DisplayName("generateTokenWithClaims should set correct expiration time")
    void generateTokenWithClaims_shouldSetCorrectExpiration() {
        String username = TEST_EMAIL;
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtTokenProvider.generateTokenWithClaims(username, null, null);

        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();

        assertThat(issuedAt.getTime()).isLessThanOrEqualTo(beforeGeneration + 1000);
        assertThat(expiration.getTime()).isGreaterThan(issuedAt.getTime());
        assertThat(expiration.getTime() - issuedAt.getTime()).isEqualTo(jwtExpirationMs);
    }

    @Test
    @DisplayName("generateRefreshToken should create valid refresh token")
    void generateRefreshToken_withUsername_shouldCreateValidToken() {
        String username = TEST_EMAIL;

        String token = jwtTokenProvider.generateRefreshToken(username);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateRefreshToken should set longer expiration time")
    void generateRefreshToken_shouldSetLongerExpiration() {
        String username = TEST_EMAIL;
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtTokenProvider.generateRefreshToken(username);

        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();

        assertThat(issuedAt.getTime()).isLessThanOrEqualTo(beforeGeneration + 1000);
        assertThat(expiration.getTime() - issuedAt.getTime()).isEqualTo(jwtRefreshExpirationMs);
    }

    @Test
    @DisplayName("generateToken should create token from Authentication")
    void generateToken_withAuthentication_shouldCreateValidToken() {
        String username = TEST_EMAIL;
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        Claims claims = parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(username);
    }

    @Test
    @DisplayName("generateTokenFromUsername should create valid token")
    void generateTokenFromUsername_withUsername_shouldCreateValidToken() {
        String username = TEST_EMAIL;

        String token = jwtTokenProvider.generateTokenFromUsername(username);

        assertThat(token).isNotNull();
        Claims claims = parseToken(token);
        assertThat(claims.getSubject()).isEqualTo(username);
    }

    @Test
    @DisplayName("validateToken should return true for valid token")
    void validateToken_withValidToken_shouldReturnTrue() {
        String username = TEST_EMAIL;
        String token = jwtTokenProvider.generateTokenWithClaims(username, null, null);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken should return false for expired token")
    void validateToken_withExpiredToken_shouldReturnFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, JWT_EXPIRATION_FIELD, -1000L);
        String username = TEST_EMAIL;
        String token = jwtTokenProvider.generateTokenWithClaims(username, null, null);

        ReflectionTestUtils.setField(jwtTokenProvider, JWT_EXPIRATION_FIELD, jwtExpirationMs);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken should return false for malformed token")
    void validateToken_withMalformedToken_shouldReturnFalse() {
        String malformedToken = "invalid.token.here";

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
    @DisplayName("validateToken should return false for null token")
    void validateToken_withNullToken_shouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("getUsernameFromToken should extract username correctly")
    void getUsernameFromToken_withValidToken_shouldExtractUsername() {
        String username = TEST_EMAIL;
        String token = jwtTokenProvider.generateTokenWithClaims(username, null, null);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("getUsernameFromToken should extract username from token with claims")
    void getUsernameFromToken_withTokenWithClaims_shouldExtractUsername() {
        String username = ADMIN_EMAIL;
        Long userId = 456L;
        String role = "ADMIN";
        String token = jwtTokenProvider.generateTokenWithClaims(username, userId, role);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("token should contain issued at timestamp")
    void generateTokenWithClaims_shouldContainIssuedAt() {
        String username = TEST_EMAIL;
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtTokenProvider.generateTokenWithClaims(username, null, null);

        Claims claims = parseToken(token);
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getIssuedAt().getTime()).isLessThanOrEqualTo(beforeGeneration + 1000);
        assertThat(claims.getIssuedAt().getTime()).isLessThanOrEqualTo(System.currentTimeMillis() + 1000);
    }

    @Test
    @DisplayName("generateTokenWithClaims should handle userId claim correctly")
    void generateTokenWithClaims_withUserId_shouldIncludeUserIdClaim() {
        String username = TEST_EMAIL;
        Long userId = 999L;

        String token = jwtTokenProvider.generateTokenWithClaims(username, userId, null);

        Claims claims = parseToken(token);
        assertThat(claims.get(USER_ID_CLAIM, Long.class)).isEqualTo(userId);
    }

    @Test
    @DisplayName("generateTokenWithClaims should handle role claim correctly")
    void generateTokenWithClaims_withRole_shouldIncludeRoleClaim() {
        String username = TEST_EMAIL;
        String role = "CUSTOMER";

        String token = jwtTokenProvider.generateTokenWithClaims(username, null, role);

        Claims claims = parseToken(token);
        List<String> roles = claims.get(ROLES_CLAIM, List.class);
        assertThat(roles).containsExactly(role);
    }

    @Test
    @DisplayName("generateTokenWithClaims should not include userId claim when null")
    void generateTokenWithClaims_withNullUserId_shouldNotIncludeUserIdClaim() {
        String username = TEST_EMAIL;

        String token = jwtTokenProvider.generateTokenWithClaims(username, null, "CUSTOMER");

        Claims claims = parseToken(token);
        assertThat(claims.get(USER_ID_CLAIM)).isNull();
    }

    @Test
    @DisplayName("generateTokenWithClaims should not include roles claim when null")
    void generateTokenWithClaims_withNullRole_shouldNotIncludeRolesClaim() {
        String username = TEST_EMAIL;

        String token = jwtTokenProvider.generateTokenWithClaims(username, 123L, null);

        Claims claims = parseToken(token);
        assertThat(claims.get(ROLES_CLAIM)).isNull();
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
