package com.bookstore.order.batch;

import com.bookstore.common.dto.OrderReportDto;
import com.bookstore.order.document.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderReportJobConfig Unit Tests")
class OrderReportJobConfigTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private ItemReader<Order> orderReader;

    @Mock
    private ItemWriter<OrderReportDto> orderReportWriter;

    private OrderReportJobConfig config;

    @BeforeEach
    void setUp() {
        JobBuilderFactory jobBuilderFactory = new JobBuilderFactory(jobRepository);
        StepBuilderFactory stepBuilderFactory = new StepBuilderFactory(jobRepository, transactionManager);
        MainframeOrderProcessor processor = new MainframeOrderProcessor();

        config = new OrderReportJobConfig(jobBuilderFactory, stepBuilderFactory, processor, mongoTemplate);
    }

    @Test
    @DisplayName("mainframeLineAggregator should return MainframeOrderLineAggregator instance")
    void mainframeLineAggregator_shouldReturnCorrectType() {
        LineAggregator<OrderReportDto> aggregator = config.mainframeLineAggregator();

        assertThat(aggregator).isNotNull();
        assertThat(aggregator).isInstanceOf(MainframeOrderLineAggregator.class);
    }

    @Test
    @DisplayName("orderSkipListener should return new instance")
    void orderSkipListener_shouldReturnNewInstance() {
        OrderSkipListener listener = config.orderSkipListener();

        assertThat(listener).isNotNull();
    }

    @Test
    @DisplayName("orderJobExecutionListener should return new instance")
    void orderJobExecutionListener_shouldReturnNewInstance() {
        OrderJobExecutionListener listener = config.orderJobExecutionListener();

        assertThat(listener).isNotNull();
    }

    @Test
    @DisplayName("orderReportStep should be created with correct name")
    void orderReportStep_shouldBeCreatedWithCorrectName() {
        Step step = config.orderReportStep(orderReader, orderReportWriter);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("orderReportStep");
    }

    @Test
    @DisplayName("orderReportJob should be created with correct name")
    void orderReportJob_shouldBeCreatedWithCorrectName() {
        Step step = config.orderReportStep(orderReader, orderReportWriter);
        Job job = config.orderReportJob(step);

        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("orderReportJob");
    }
}
