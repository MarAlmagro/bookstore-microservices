package com.bookstore.order.security;

import com.bookstore.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    public boolean isOrderOwner(String orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    public boolean isOrderOwnerOrAdmin(String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        Long userId = getCurrentUserId();
        return userId != null && orderRepository.existsByIdAndUserId(orderId, userId);
    }

    public boolean isSameUserOrAdmin(Long requestedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return true;
        }

        Long userId = getCurrentUserId();
        return userId != null && userId.equals(requestedUserId);
    }

    private Long getCurrentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            return (Long) request.getAttribute("userId");
        }
        return null;
    }
}
