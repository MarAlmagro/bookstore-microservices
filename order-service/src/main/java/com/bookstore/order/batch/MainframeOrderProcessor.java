package com.bookstore.order.batch;

import com.bookstore.common.dto.OrderReportDto;
import com.bookstore.common.exception.MalformedDataException;
import com.bookstore.order.document.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true", matchIfMissing = true)
public class MainframeOrderProcessor implements ItemProcessor<Order, OrderReportDto> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private long sequenceCounter = 1;

    @Override
    public OrderReportDto process(Order order) throws Exception {
        try {
            if (order.getId() == null || order.getTotalAmount() == null || order.getCreatedAt() == null) {
                throw new MalformedDataException("Order missing required fields: " + order.getId());
            }

            String category = extractCategory(order);
            String formattedDate = order.getCreatedAt().format(DATE_FORMATTER);
            
            OrderReportDto dto = OrderReportDto.builder()
                    .numericId(sequenceCounter++)
                    .category(category)
                    .totalAmount(order.getTotalAmount())
                    .formattedDate(formattedDate)
                    .build();
            
            log.debug("Processed order {} to report Dto", order.getId());
            return dto;
            
        } catch (Exception e) {
            log.error("Failed to process order {}: {}", order.getId(), e.getMessage());
            throw new MalformedDataException("Failed to process order: " + order.getId(), e);
        }
    }

    private String extractCategory(Order order) {
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            return "GENERAL";
        }
        return "UNKNOWN";
    }
}
