package com.bookstore.catalog.integration;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.batch.job.enabled=true"
})
class CatalogImportJobIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job catalogImportJob;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        bookRepository.deleteAll();
        testFile = File.createTempFile("catalog_import_test", ".csv");
        testFile.deleteOnExit();
    }

    @AfterEach
    void tearDown() {
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    void catalogImportJob_ShouldCreateNewBooks_WhenIsbnDoesNotExist() throws Exception {
        createTestFile(
                "isbn,title,author,description,price,stock,category\n" +
                        "978-0-111111-11-1,Test Book 1,Author 1,Description 1,29.99,100,Fiction\n" +
                        "978-0-222222-22-2,Test Book 2,Author 2,Description 2,39.99,200,NonFiction\n");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", testFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(catalogImportJob, jobParameters);

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        List<Book> books = bookRepository.findAll();
        assertEquals(2, books.size());

        Optional<Book> book1 = bookRepository.findByIsbn("978-0-111111-11-1");
        assertTrue(book1.isPresent());
        assertEquals("Test Book 1", book1.get().getTitle());
        assertEquals(new BigDecimal("29.99"), book1.get().getPrice());
        assertEquals(100, book1.get().getStock());
    }

    @Test
    void catalogImportJob_ShouldUpdateExistingBooks_WhenIsbnExists() throws Exception {
        Book existingBook = Book.builder()
                .isbn("978-0-333333-33-3")
                .title("Old Title")
                .author("Old Author")
                .description("Old Description")
                .price(new BigDecimal("19.99"))
                .stock(50)
                .category("OldCategory")
                .build();
        bookRepository.save(existingBook);

        createTestFile(
                "isbn,title,author,description,price,stock,category\n" +
                        "978-0-333333-33-3,Updated Title,Updated Author,Updated Description,49.99,150,UpdatedCategory\n");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", testFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(catalogImportJob, jobParameters);

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        Optional<Book> updatedBook = bookRepository.findByIsbn("978-0-333333-33-3");
        assertTrue(updatedBook.isPresent());
        assertEquals("Updated Title", updatedBook.get().getTitle());
        assertEquals("Updated Author", updatedBook.get().getAuthor());
        assertEquals(new BigDecimal("49.99"), updatedBook.get().getPrice());
        assertEquals(150, updatedBook.get().getStock());
        assertEquals("UpdatedCategory", updatedBook.get().getCategory());
    }

    @Test
    void catalogImportJob_ShouldPersistBatchMetadata_InJobRepository() throws Exception {
        createTestFile(
                "isbn,title,author,description,price,stock,category\n" +
                        "978-0-444444-44-4,Metadata Test,Author,Description,25.00,75,Test\n");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", testFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(catalogImportJob, jobParameters);

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        Integer jobInstanceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_NAME = 'catalogImportJob'",
                Integer.class);
        assertNotNull(jobInstanceCount);
        assertTrue(jobInstanceCount > 0, "BATCH_JOB_INSTANCE should contain job instances");

        Integer jobExecutionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_JOB_EXECUTION WHERE JOB_INSTANCE_ID IN " +
                        "(SELECT JOB_INSTANCE_ID FROM BATCH_JOB_INSTANCE WHERE JOB_NAME = 'catalogImportJob')",
                Integer.class);
        assertNotNull(jobExecutionCount);
        assertTrue(jobExecutionCount > 0, "BATCH_JOB_EXECUTION should contain executions");

        Integer stepExecutionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE STEP_NAME = 'catalogImportStep'",
                Integer.class);
        assertNotNull(stepExecutionCount);
        assertTrue(stepExecutionCount > 0, "BATCH_STEP_EXECUTION should contain step executions");
    }

    @Test
    void catalogImportJob_ShouldHandleMultipleRecords_WithUpsertLogic() throws Exception {
        Book existingBook1 = Book.builder()
                .isbn("978-0-555555-55-5")
                .title("Existing Book 1")
                .author("Author 1")
                .description("Description 1")
                .price(new BigDecimal("15.00"))
                .stock(25)
                .category("Category1")
                .build();
        bookRepository.save(existingBook1);

        createTestFile(
                "isbn,title,author,description,price,stock,category\n" +
                        "978-0-555555-55-5,Updated Existing,Author 1 Updated,Description 1 Updated,35.00,125,Category1Updated\n"
                        +
                        "978-0-666666-66-6,New Book,Author 2,Description 2,45.00,200,Category2\n" +
                        "978-0-777777-77-7,Another New Book,Author 3,Description 3,55.00,300,Category3\n");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", testFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(catalogImportJob, jobParameters);

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        List<Book> allBooks = bookRepository.findAll();
        assertEquals(3, allBooks.size());

        Optional<Book> updatedExisting = bookRepository.findByIsbn("978-0-555555-55-5");
        assertTrue(updatedExisting.isPresent());
        assertEquals("Updated Existing", updatedExisting.get().getTitle());
        assertEquals(new BigDecimal("35.00"), updatedExisting.get().getPrice());

        Optional<Book> newBook1 = bookRepository.findByIsbn("978-0-666666-66-6");
        assertTrue(newBook1.isPresent());
        assertEquals("New Book", newBook1.get().getTitle());

        Optional<Book> newBook2 = bookRepository.findByIsbn("978-0-777777-77-7");
        assertTrue(newBook2.isPresent());
        assertEquals("Another New Book", newBook2.get().getTitle());
    }

    @Test
    void catalogImportJob_ShouldProcessExactNumberOfItems() throws Exception {
        createTestFile(
                "isbn,title,author,description,price,stock,category\n" +
                        "978-0-888888-88-8,Book 1,Author 1,Desc 1,10.00,10,Cat1\n" +
                        "978-0-999999-99-9,Book 2,Author 2,Desc 2,20.00,20,Cat2\n" +
                        "978-1-000000-00-0,Book 3,Author 3,Desc 3,30.00,30,Cat3\n" +
                        "978-1-111111-11-1,Book 4,Author 4,Desc 4,40.00,40,Cat4\n" +
                        "978-1-222222-22-2,Book 5,Author 5,Desc 5,50.00,50,Cat5\n");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFile", testFile.getAbsolutePath())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(catalogImportJob, jobParameters);

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertEquals(5, stepExecution.getReadCount(), "Should read 5 items");
        assertEquals(5, stepExecution.getWriteCount(), "Should write 5 items");
        assertEquals(0, stepExecution.getSkipCount(), "Should skip 0 items");

        assertEquals(5, bookRepository.count());
    }

    private void createTestFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(content);
        }
    }
}
