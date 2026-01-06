package com.bookstore.order.batch;

import com.bookstore.common.dto.OrderReportDTO;
import org.springframework.batch.item.file.transform.LineAggregator;

public class MainframeOrderLineAggregator implements LineAggregator<OrderReportDTO> {
    
    @Override
    public String aggregate(OrderReportDTO item) {
        return String.format(
            "01" +                          
            "%010d" +                       
            "%-15.15s" +                    
            "%012d" +                       
            "%8s",                          
            item.getNumericId(),
            item.getCategory(),
            Math.round(item.getTotalAmount().doubleValue() * 100),
            item.getFormattedDate()
        );
    }
}
