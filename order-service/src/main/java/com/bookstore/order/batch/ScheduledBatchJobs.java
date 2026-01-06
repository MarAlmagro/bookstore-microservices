package com.bookstore.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledBatchJobs {

    private final JobLauncher jobLauncher;
    private final Job orderReportJob;

    @Scheduled(cron = "0 0 0 * * *")
    public void runNightlySalesReport() {
        try {
            log.info("Starting scheduled nightly sales report job");
            
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(1);
            
            String outputFile = String.format("order-service/exports/sales_report_%s.txt",
                                            endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startDate.format(formatter))
                    .addString("endDate", endDate.format(formatter))
                    .addString("outputFile", outputFile)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(orderReportJob, jobParameters);
            
            log.info("Nightly sales report job completed successfully");
            
        } catch (Exception e) {
            log.error("Failed to run nightly sales report job", e);
        }
    }
}
