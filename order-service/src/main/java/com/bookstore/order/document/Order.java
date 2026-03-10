package com.bookstore.order.document;

import com.bookstore.common.constants.OrderStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "orders")
@CompoundIndexes({
    @CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}"),
    @CompoundIndex(name = "user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class Order {

    @Id
    private String id;

    @NotNull(message = "User ID is required")
    @Indexed
    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @lombok.Setter(lombok.AccessLevel.NONE)
    @lombok.Getter(lombok.AccessLevel.NONE)
    private List<OrderItem> items;

    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Order status is required")
    @Indexed
    private OrderStatus status;

    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String shippingAddress;
    private String customerEmail;
    private String customerName;

    @Builder
    public Order(String id, Long userId, @Singular List<OrderItem> items, BigDecimal totalAmount, OrderStatus status,
            LocalDateTime createdAt, LocalDateTime updatedAt, String shippingAddress, String customerEmail,
            String customerName) {
        this.id = id;
        this.userId = userId;
        this.items = items != null ? new ArrayList<>(items) : null;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.shippingAddress = shippingAddress;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? new ArrayList<>(items) : null;
    }

    public List<OrderItem> getItems() {
        return items != null ? new ArrayList<>(items) : null;
    }
}
