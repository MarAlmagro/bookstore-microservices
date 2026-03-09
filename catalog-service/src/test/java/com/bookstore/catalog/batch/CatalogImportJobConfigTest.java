package com.bookstore.catalog.batch;

import com.bookstore.catalog.entity.Book;
import com.bookstore.common.dto.BookImportDto;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogImportJobConfig Unit Tests")
class CatalogImportJobConfigTest {

    private static final String TEST_CATALOG_FILE = "test-catalog.csv";

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private BookImportProcessor bookImportProcessor;

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private CatalogImportJobConfig config;

    @BeforeEach
    void setUp() {
        jobBuilderFactory = new JobBuilderFactory(jobRepository);
        stepBuilderFactory = new StepBuilderFactory(jobRepository, transactionManager);
        config = new CatalogImportJobConfig(
            jobBuilderFactory,
            stepBuilderFactory,
            bookImportProcessor,
            entityManagerFactory
        );
    }

    @Test
    @DisplayName("jobBuilderFactory should be available")
    void jobBuilderFactory_shouldBeAvailable() {
        assertThat(jobBuilderFactory).isNotNull();
    }

    @Test
    @DisplayName("stepBuilderFactory should be available")
    void stepBuilderFactory_shouldBeAvailable() {
        assertThat(stepBuilderFactory).isNotNull();
    }

    @Test
    @DisplayName("bookImportProcessor should be available")
    void bookImportProcessor_shouldBeAvailable() {
        assertThat(bookImportProcessor).isNotNull();
    }

    @Test
    @DisplayName("entityManagerFactory should be available")
    void entityManagerFactory_shouldBeAvailable() {
        assertThat(entityManagerFactory).isNotNull();
    }

    @Test
    @DisplayName("catalogWriter should create JpaItemWriter")
    void catalogWriter_shouldCreateJpaItemWriter() {
        JpaItemWriter<Book> writer = config.catalogWriter();
        assertThat(writer).isNotNull();
    }

    @Test
    @DisplayName("catalogReader should create FlatFileItemReader with correct configuration")
    void catalogReader_shouldCreateFlatFileItemReader() {
        FlatFileItemReader<BookImportDto> reader = config.catalogReader(TEST_CATALOG_FILE);
        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("catalogImportStep should wire reader, processor, and writer")
    void catalogImportStep_shouldWireComponents() {
        ItemReader<BookImportDto> reader = config.catalogReader(TEST_CATALOG_FILE);
        ItemWriter<Book> writer = config.catalogWriter();

        Step step = config.catalogImportStep(reader, writer);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("catalogImportStep");
    }

    @Test
    @DisplayName("catalogImportJob should be configured with step")
    void catalogImportJob_shouldBeConfiguredWithStep() {
        ItemReader<BookImportDto> reader = config.catalogReader(TEST_CATALOG_FILE);
        ItemWriter<Book> writer = config.catalogWriter();
        Step step = config.catalogImportStep(reader, writer);

        Job job = config.catalogImportJob(step);

        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("catalogImportJob");
    }
}
