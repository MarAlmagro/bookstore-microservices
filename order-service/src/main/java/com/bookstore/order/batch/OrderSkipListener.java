package com.bookstore.order.batch;

import com.bookstore.order.document.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class OrderSkipListener implements SkipListener<Order, Object> {

    private static final String REJECTED_DIR = "exports/rejected";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Skipped record during read: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Skipped record during write: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Order order, Throwable t) {
        log.warn("Skipped order during processing: orderId={}, error={}", 
                 order != null ? order.getId() : "unknown", t.getMessage());
        
        if (order != null) {
            writeToDeadLetterFile(order, t);
        }
    }

    private void writeToDeadLetterFile(Order order, Throwable t) {
        try {
            File rejectedDir = new File(REJECTED_DIR);
            if (!rejectedDir.exists()) {
                rejectedDir.mkdirs();
            }

            String fileName = String.format("rejected_orders_%s.log", 
                                          LocalDateTime.now().format(FILE_DATE_FORMATTER));
            File deadLetterFile = new File(rejectedDir, fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(deadLetterFile, true))) {
                writer.write(String.format("[%s] OrderId: %s, Error: %s, Details: %s%n",
                                         LocalDateTime.now(),
                                         order.getId(),
                                         t.getClass().getSimpleName(),
                                         t.getMessage()));
            }

            log.info("Written rejected order {} to dead letter file: {}", 
                    order.getId(), deadLetterFile.getAbsolutePath());

        } catch (IOException e) {
            log.error("Failed to write to dead letter file", e);
        }
    }
}
