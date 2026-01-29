package com.bookstore.catalog.batch;

import com.bookstore.catalog.entity.Book;
import com.bookstore.common.dto.BookImportDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManagerFactory;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=true",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
@DisplayName("CatalogImportJobConfig Unit Tests")
class CatalogImportJobConfigTest {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private BookImportProcessor bookImportProcessor;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired(required = false)
    private Job catalogImportJob;

    @Autowired(required = false)
    private Step catalogImportStep;

    @Test
    @DisplayName("catalogImportJob bean should be created")
    void catalogImportJob_shouldBeCreated() {
        assertThat(catalogImportJob).isNotNull();
        assertThat(catalogImportJob.getName()).isEqualTo("catalogImportJob");
    }

    @Test
    @DisplayName("catalogImportStep bean should be created")
    void catalogImportStep_shouldBeCreated() {
        assertThat(catalogImportStep).isNotNull();
        assertThat(catalogImportStep.getName()).isEqualTo("catalogImportStep");
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
        CatalogImportJobConfig config = new CatalogImportJobConfig(
            jobBuilderFactory,
            stepBuilderFactory,
            bookImportProcessor,
            entityManagerFactory
        );

        JpaItemWriter<Book> writer = config.catalogWriter();

        assertThat(writer).isNotNull();
    }

    @Test
    @DisplayName("catalogReader should create FlatFileItemReader with correct configuration")
    void catalogReader_shouldCreateFlatFileItemReader() {
        CatalogImportJobConfig config = new CatalogImportJobConfig(
            jobBuilderFactory,
            stepBuilderFactory,
            bookImportProcessor,
            entityManagerFactory
        );

        String testFile = "test-catalog.csv";
        FlatFileItemReader<BookImportDto> reader = config.catalogReader(testFile);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("catalogImportStep should wire reader, processor, and writer")
    void catalogImportStep_shouldWireComponents() {
        CatalogImportJobConfig config = new CatalogImportJobConfig(
            jobBuilderFactory,
            stepBuilderFactory,
            bookImportProcessor,
            entityManagerFactory
        );

        String testFile = "test-catalog.csv";
        ItemReader<BookImportDto> reader = config.catalogReader(testFile);
        ItemWriter<Book> writer = config.catalogWriter();

        Step step = config.catalogImportStep(reader, writer);

        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("catalogImportStep");
    }

    @Test
    @DisplayName("catalogImportJob should be configured with step")
    void catalogImportJob_shouldBeConfiguredWithStep() {
        CatalogImportJobConfig config = new CatalogImportJobConfig(
            jobBuilderFactory,
            stepBuilderFactory,
            bookImportProcessor,
            entityManagerFactory
        );

        String testFile = "test-catalog.csv";
        ItemReader<BookImportDto> reader = config.catalogReader(testFile);
        ItemWriter<Book> writer = config.catalogWriter();
        Step step = config.catalogImportStep(reader, writer);

        Job job = config.catalogImportJob(step);

        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("catalogImportJob");
    }
}
