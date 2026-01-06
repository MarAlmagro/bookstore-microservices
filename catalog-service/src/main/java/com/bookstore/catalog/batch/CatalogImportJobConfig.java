package com.bookstore.catalog.batch;

import com.bookstore.catalog.entity.Book;
import com.bookstore.common.dto.BookImportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CatalogImportJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BookImportProcessor bookImportProcessor;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<BookImportDto> catalogReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        
        FlatFileItemReader<BookImportDto> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLinesToSkip(1);
        
        DefaultLineMapper<BookImportDto> lineMapper = new DefaultLineMapper<>();
        
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("isbn", "title", "author", "description", "price", "stock", "category");
        tokenizer.setDelimiter(",");
        
        BeanWrapperFieldSetMapper<BookImportDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(BookImportDto.class);
        
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        
        reader.setLineMapper(lineMapper);
        
        return reader;
    }

    @Bean
    public JpaItemWriter<Book> catalogWriter() {
        JpaItemWriter<Book> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step catalogImportStep() {
        return stepBuilderFactory.get("catalogImportStep")
                .<BookImportDto, Book>chunk(100)
                .reader(catalogReader(null))
                .processor(bookImportProcessor)
                .writer(catalogWriter())
                .build();
    }

    @Bean
    public Job catalogImportJob() {
        return jobBuilderFactory.get("catalogImportJob")
                .incrementer(new RunIdIncrementer())
                .start(catalogImportStep())
                .build();
    }
}
