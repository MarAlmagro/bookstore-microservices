package com.bookstore.admin.controller;

import com.bookstore.admin.client.CatalogClient;
import com.bookstore.admin.client.OrderClient;
import com.bookstore.admin.dto.BookDTO;
import com.bookstore.admin.dto.DashboardStatsDTO;
import com.bookstore.admin.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CatalogClient catalogClient;
    private final OrderClient orderClient;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            List<BookDTO> books = catalogClient.getAllBooks();
            List<OrderDTO> orders = orderClient.getAllOrders();
            
            BigDecimal totalRevenue = orders.stream()
                    .map(OrderDTO::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            DashboardStatsDTO stats = new DashboardStatsDTO(
                    (long) books.size(),
                    (long) orders.size(),
                    0L,
                    totalRevenue
            );
            
            model.addAttribute("stats", stats);
            model.addAttribute("recentOrders", orders.stream().limit(5).toArray());
            model.addAttribute("topBooks", books.stream().limit(5).toArray());
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard data: " + e.getMessage());
        }
        
        return "dashboard";
    }
}
