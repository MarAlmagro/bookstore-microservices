package com.bookstore.catalog.controller;

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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Operations", description = "Batch processing endpoints for catalog operations")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job catalogImportJob;

    @PostMapping("/import-catalog")
    @Operation(summary = "Import catalog from CSV file", 
               description = "Triggers batch job to import books from CSV file with upsert logic")
    public ResponseEntity<Map<String, Object>> importCatalog(
            @RequestParam String inputFile) {
        
        try {
            log.info("Starting catalog import job for file: {}", inputFile);
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFile", inputFile)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(catalogImportJob, jobParameters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Catalog import job started successfully");
            response.put("inputFile", inputFile);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to start catalog import job", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "FAILED");
            response.put("message", "Failed to start catalog import job: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
