package com.bookstore.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Operations", description = "Batch processing endpoints for order operations")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job orderReportJob;

    @PostMapping("/generate-sales-report")
    @Operation(summary = "Generate sales report", 
               description = "Triggers batch job to generate mainframe-ready fixed-length order report")
    public ResponseEntity<Map<String, Object>> generateSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String outputFile) {
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            
            LocalDateTime start = startDate != null 
                ? LocalDateTime.parse(startDate, formatter)
                : LocalDateTime.now().minusDays(1);
            
            LocalDateTime end = endDate != null
                ? LocalDateTime.parse(endDate, formatter)
                : LocalDateTime.now();
            
            String output = outputFile != null
                ? outputFile
                : String.format("exports/sales_report_%s.txt", 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            log.info("Starting sales report job from {} to {}", start, end);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", start.format(formatter))
                    .addString("endDate", end.format(formatter))
                    .addString("outputFile", output)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(orderReportJob, jobParameters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Sales report job started successfully");
            response.put("startDate", start.toString());
            response.put("endDate", end.toString());
            response.put("outputFile", output);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to start sales report job", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "FAILED");
            response.put("message", "Failed to start sales report job: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
