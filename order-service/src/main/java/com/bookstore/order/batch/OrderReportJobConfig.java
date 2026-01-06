package com.bookstore.order.batch;

import com.bookstore.common.dto.OrderReportDto;
import com.bookstore.common.exception.MalformedDataException;
import com.bookstore.order.document.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderReportJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MainframeOrderProcessor mainframeOrderProcessor;
    private final MongoTemplate mongoTemplate;

    @Bean
    @StepScope
    public MongoOrderItemReader orderReader(
            @Value("#{jobParameters['startDate']}") String startDateStr,
            @Value("#{jobParameters['endDate']}") String endDateStr) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
        LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

        return new MongoOrderItemReader(mongoTemplate, startDate, endDate);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<OrderReportDto> orderReportWriter(
            @Value("#{jobParameters['outputFile']}") String outputFile) {

        FlatFileItemWriter<OrderReportDto> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputFile));
        writer.setEncoding(StandardCharsets.UTF_8.name());
        writer.setLineSeparator("\r\n");
        writer.setLineAggregator(mainframeLineAggregator());

        return writer;
    }

    @Bean
    public LineAggregator<OrderReportDto> mainframeLineAggregator() {
        return new MainframeOrderLineAggregator();
    }

    @Bean
    public Step orderReportStep(
            ItemReader<Order> orderReader,
            ItemWriter<OrderReportDto> orderReportWriter) {
        return stepBuilderFactory.get("orderReportStep")
                .<Order, OrderReportDto>chunk(100)
                .reader(orderReader)
                .processor(mainframeOrderProcessor)
                .writer(orderReportWriter)
                .faultTolerant()
                .skip(MalformedDataException.class)
                .skipLimit(10)
                .listener(orderSkipListener())
                .listener(orderJobExecutionListener())
                .build();
    }

    @Bean
    public Job orderReportJob(Step orderReportStep) {
        return jobBuilderFactory.get("orderReportJob")
                .incrementer(new RunIdIncrementer())
                .start(orderReportStep)
                .build();
    }

    @Bean
    public OrderSkipListener orderSkipListener() {
        return new OrderSkipListener();
    }

    @Bean
    public OrderJobExecutionListener orderJobExecutionListener() {
        return new OrderJobExecutionListener();
    }
}
