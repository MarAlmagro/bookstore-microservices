package com.bookstore.order.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class OrderJobExecutionListener implements JobExecutionListener {

    private LocalDateTime startTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = LocalDateTime.now();
        log.info("========================================");
        log.info("Starting Job: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job ID: {}", jobExecution.getJobId());
        log.info("Start Time: {}", startTime);
        log.info("========================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        
        log.info("========================================");
        log.info("Job Completed: {}", jobExecution.getJobInstance().getJobName());
        log.info("Job ID: {}", jobExecution.getJobId());
        log.info("Status: {}", jobExecution.getStatus());
        log.info("End Time: {}", endTime);
        log.info("Execution Time: {} seconds", duration.getSeconds());
        
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("Step: {}", stepExecution.getStepName());
            log.info("  - Read Count: {}", stepExecution.getReadCount());
            log.info("  - Write Count: {}", stepExecution.getWriteCount());
            log.info("  - Skip Count: {}", stepExecution.getSkipCount());
            log.info("  - Commit Count: {}", stepExecution.getCommitCount());
        });
        
        log.info("========================================");
    }
}
