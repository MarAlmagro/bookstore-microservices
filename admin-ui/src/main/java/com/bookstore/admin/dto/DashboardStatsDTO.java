package com.bookstore.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalBooks;
    private Long totalOrders;
    private Long totalUsers;
    private BigDecimal totalRevenue;
}
