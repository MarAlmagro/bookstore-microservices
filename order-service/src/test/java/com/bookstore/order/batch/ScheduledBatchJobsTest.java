package com.bookstore.order.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledBatchJobs Unit Tests")
class ScheduledBatchJobsTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job orderReportJob;

    @InjectMocks
    private ScheduledBatchJobs scheduledBatchJobs;

    @Test
    @DisplayName("runNightlySalesReport should launch job with correct parameters")
    void runNightlySalesReport_shouldLaunchJobWithParameters() throws Exception {
        scheduledBatchJobs.runNightlySalesReport();

        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(orderReportJob), paramsCaptor.capture());

        JobParameters params = paramsCaptor.getValue();
        assertThat(params.getString("startDate")).isNotNull();
        assertThat(params.getString("endDate")).isNotNull();
        assertThat(params.getString("outputFile")).startsWith("order-service/exports/sales_report_");
        assertThat(params.getLong("timestamp")).isGreaterThan(0);
    }

    @Test
    @DisplayName("runNightlySalesReport should not throw when job launcher fails")
    void runNightlySalesReport_whenJobLauncherFails_shouldNotThrow() throws Exception {
        doThrow(new RuntimeException("Job failed")).when(jobLauncher).run(any(), any());

        scheduledBatchJobs.runNightlySalesReport();

        verify(jobLauncher).run(eq(orderReportJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("runNightlySalesReport should use ISO_LOCAL_DATE_TIME format for dates")
    void runNightlySalesReport_shouldUseIsoDateTimeFormat() throws Exception {
        scheduledBatchJobs.runNightlySalesReport();

        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(orderReportJob), paramsCaptor.capture());

        JobParameters params = paramsCaptor.getValue();
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        assertThat(startDate).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*");
        assertThat(endDate).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*");
    }

    @Test
    @DisplayName("runNightlySalesReport should set outputFile with date pattern")
    void runNightlySalesReport_shouldSetOutputFileWithDatePattern() throws Exception {
        scheduledBatchJobs.runNightlySalesReport();

        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobLauncher).run(eq(orderReportJob), paramsCaptor.capture());

        String outputFile = paramsCaptor.getValue().getString("outputFile");
        assertThat(outputFile).matches("order-service/exports/sales_report_\\d{8}\\.txt");
    }
}
