package com.bookstore.order.security;

import com.bookstore.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderSecurityService Unit Tests")
class OrderSecurityServiceTest {

    private static final String ORDER_ID = "order1";
    private static final String USER_ID_ATTR = "userId";
    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    private static final SimpleGrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderSecurityService orderSecurityService;

    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("isOrderOwner should return true when user owns the order")
    void isOrderOwner_whenUserOwnsOrder_shouldReturnTrue() {
        when(orderRepository.existsByIdAndUserId(ORDER_ID, 1L)).thenReturn(true);

        boolean result = orderSecurityService.isOrderOwner(ORDER_ID, 1L);

        assertThat(result).isTrue();
        verify(orderRepository).existsByIdAndUserId(ORDER_ID, 1L);
    }

    @Test
    @DisplayName("isOrderOwner should return false when user does not own the order")
    void isOrderOwner_whenUserDoesNotOwnOrder_shouldReturnFalse() {
        when(orderRepository.existsByIdAndUserId(ORDER_ID, 2L)).thenReturn(false);

        boolean result = orderSecurityService.isOrderOwner(ORDER_ID, 2L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isOrderOwnerOrAdmin should return false when authentication is null")
    void isOrderOwnerOrAdmin_whenNoAuthentication_shouldReturnFalse() {
        SecurityContextHolder.clearContext();

        boolean result = orderSecurityService.isOrderOwnerOrAdmin(ORDER_ID);

        assertThat(result).isFalse();
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("isOrderOwnerOrAdmin should return true when user is admin")
    void isOrderOwnerOrAdmin_whenAdmin_shouldReturnTrue() {
        setAuthentication("admin", List.of(ROLE_ADMIN));

        boolean result = orderSecurityService.isOrderOwnerOrAdmin(ORDER_ID);

        assertThat(result).isTrue();
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("isOrderOwnerOrAdmin should return true when user owns the order")
    void isOrderOwnerOrAdmin_whenOwner_shouldReturnTrue() {
        setAuthentication("user", List.of(ROLE_USER));
        mockRequest.setAttribute(USER_ID_ATTR, 1L);
        when(orderRepository.existsByIdAndUserId(ORDER_ID, 1L)).thenReturn(true);

        boolean result = orderSecurityService.isOrderOwnerOrAdmin(ORDER_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isOrderOwnerOrAdmin should return false when regular user does not own order")
    void isOrderOwnerOrAdmin_whenNotOwner_shouldReturnFalse() {
        setAuthentication("user", List.of(ROLE_USER));
        mockRequest.setAttribute(USER_ID_ATTR, 2L);
        when(orderRepository.existsByIdAndUserId(ORDER_ID, 2L)).thenReturn(false);

        boolean result = orderSecurityService.isOrderOwnerOrAdmin(ORDER_ID);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isOrderOwnerOrAdmin should return false when userId attribute is null")
    void isOrderOwnerOrAdmin_whenUserIdNull_shouldReturnFalse() {
        setAuthentication("user", List.of(ROLE_USER));

        boolean result = orderSecurityService.isOrderOwnerOrAdmin(ORDER_ID);

        assertThat(result).isFalse();
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("isSameUserOrAdmin should return false when authentication is null")
    void isSameUserOrAdmin_whenNoAuthentication_shouldReturnFalse() {
        SecurityContextHolder.clearContext();

        boolean result = orderSecurityService.isSameUserOrAdmin(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isSameUserOrAdmin should return true when user is admin")
    void isSameUserOrAdmin_whenAdmin_shouldReturnTrue() {
        setAuthentication("admin", List.of(ROLE_ADMIN));

        boolean result = orderSecurityService.isSameUserOrAdmin(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isSameUserOrAdmin should return true when same user")
    void isSameUserOrAdmin_whenSameUser_shouldReturnTrue() {
        setAuthentication("user", List.of(ROLE_USER));
        mockRequest.setAttribute(USER_ID_ATTR, 1L);

        boolean result = orderSecurityService.isSameUserOrAdmin(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isSameUserOrAdmin should return false when different user")
    void isSameUserOrAdmin_whenDifferentUser_shouldReturnFalse() {
        setAuthentication("user", List.of(ROLE_USER));
        mockRequest.setAttribute(USER_ID_ATTR, 2L);

        boolean result = orderSecurityService.isSameUserOrAdmin(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isSameUserOrAdmin should return false when userId attribute is null")
    void isSameUserOrAdmin_whenUserIdNull_shouldReturnFalse() {
        setAuthentication("user", List.of(ROLE_USER));

        boolean result = orderSecurityService.isSameUserOrAdmin(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isSameUserOrAdmin should return false when RequestContextHolder has no attributes")
    void isSameUserOrAdmin_whenNoRequestContext_shouldReturnFalse() {
        setAuthentication("user", List.of(ROLE_USER));
        RequestContextHolder.resetRequestAttributes();

        boolean result = orderSecurityService.isSameUserOrAdmin(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isOrderOwnerOrAdmin should return false when RequestContextHolder has no attributes")
    void isOrderOwnerOrAdmin_whenNoRequestContext_shouldReturnFalse() {
        setAuthentication("user", List.of(ROLE_USER));
        RequestContextHolder.resetRequestAttributes();

        boolean result = orderSecurityService.isOrderOwnerOrAdmin(ORDER_ID);

        assertThat(result).isFalse();
        verifyNoInteractions(orderRepository);
    }

    private void setAuthentication(String username, List<SimpleGrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
