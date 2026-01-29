package com.bookstore.catalog.repository;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.fixtures.BookTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("BookRepository Unit Tests")
class BookRepositoryTest {

    private static final String EFFECTIVE_JAVA = "Effective Java";
    private static final String PROGRAMMING = "Programming";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book sampleBook;
    private Book secondBook;
    private Book lowStockBook;

    @BeforeEach
    void setUp() {
        sampleBook = BookTestFixtures.createSampleBook();
        sampleBook.setId(null);
        
        secondBook = BookTestFixtures.createSecondSampleBook();
        secondBook.setId(null);
        
        lowStockBook = BookTestFixtures.createLowStockBook();
        lowStockBook.setId(null);
    }

    @Test
    @DisplayName("findByIsbn should return book when ISBN exists")
    void findByIsbn_whenIsbnExists_shouldReturnBook() {
        entityManager.persist(sampleBook);
        entityManager.flush();

        Optional<Book> result = bookRepository.findByIsbn(sampleBook.getIsbn());

        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo(sampleBook.getIsbn());
        assertThat(result.get().getTitle()).isEqualTo(EFFECTIVE_JAVA);
    }

    @Test
    @DisplayName("findByIsbn should return empty when ISBN does not exist")
    void findByIsbn_whenIsbnDoesNotExist_shouldReturnEmpty() {
        Optional<Book> result = bookRepository.findByIsbn("9999999999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByCategory should return books in category")
    void findByCategory_whenCategoryExists_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(secondBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByCategory(PROGRAMMING);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Book::getCategory).containsOnly(PROGRAMMING);
    }

    @Test
    @DisplayName("findByCategory should return empty list when category has no books")
    void findByCategory_whenCategoryEmpty_shouldReturnEmptyList() {
        List<Book> result = bookRepository.findByCategory("NonExistent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByAuthorContainingIgnoreCase should return books with partial author match")
    void findByAuthorContainingIgnoreCase_whenAuthorMatches_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(secondBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByAuthorContainingIgnoreCase("bloch");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Joshua Bloch");
    }

    @Test
    @DisplayName("findByAuthorContainingIgnoreCase should be case insensitive")
    void findByAuthorContainingIgnoreCase_withDifferentCase_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByAuthorContainingIgnoreCase("JOSHUA");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Joshua Bloch");
    }

    @Test
    @DisplayName("searchByTitleOrAuthor should find books by title")
    void searchByTitleOrAuthor_whenTitleMatches_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(secondBook);
        entityManager.flush();

        List<Book> result = bookRepository.searchByTitleOrAuthor("Effective");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(EFFECTIVE_JAVA);
    }

    @Test
    @DisplayName("searchByTitleOrAuthor should find books by author")
    void searchByTitleOrAuthor_whenAuthorMatches_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(secondBook);
        entityManager.flush();

        List<Book> result = bookRepository.searchByTitleOrAuthor("Martin");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("searchByTitleOrAuthor should be case insensitive")
    void searchByTitleOrAuthor_withDifferentCase_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.flush();

        List<Book> result = bookRepository.searchByTitleOrAuthor("EFFECTIVE");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(EFFECTIVE_JAVA);
    }

    @Test
    @DisplayName("findByStockGreaterThan should return books with stock above threshold")
    void findByStockGreaterThan_whenStockAboveThreshold_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(lowStockBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByStockGreaterThan(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStock()).isGreaterThan(10);
        assertThat(result.get(0).getTitle()).isEqualTo(EFFECTIVE_JAVA);
    }

    @Test
    @DisplayName("findByStockGreaterThan should return empty list when no books meet criteria")
    void findByStockGreaterThan_whenNoBooksMeetCriteria_shouldReturnEmptyList() {
        entityManager.persist(lowStockBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByStockGreaterThan(100);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findLowStockBooks should return books below threshold")
    void findLowStockBooks_whenBooksAreBelowThreshold_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(lowStockBook);
        entityManager.flush();

        List<Book> result = bookRepository.findLowStockBooks(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStock()).isLessThan(10);
        assertThat(result.get(0).getTitle()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    @DisplayName("findLowStockBooks should exclude out of stock books")
    void findLowStockBooks_shouldExcludeOutOfStockBooks() {
        Book outOfStockBook = BookTestFixtures.createOutOfStockBook();
        outOfStockBook.setId(null);
        
        entityManager.persist(outOfStockBook);
        entityManager.persist(lowStockBook);
        entityManager.flush();

        List<Book> result = bookRepository.findLowStockBooks(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStock()).isGreaterThan(0);
    }

    @Test
    @DisplayName("existsByIsbn should return true when ISBN exists")
    void existsByIsbn_whenIsbnExists_shouldReturnTrue() {
        entityManager.persist(sampleBook);
        entityManager.flush();

        boolean result = bookRepository.existsByIsbn(sampleBook.getIsbn());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsByIsbn should return false when ISBN does not exist")
    void existsByIsbn_whenIsbnDoesNotExist_shouldReturnFalse() {
        boolean result = bookRepository.existsByIsbn("9999999999");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("findByCategoryAndStockGreaterThan should return books in category with sufficient stock")
    void findByCategoryAndStockGreaterThan_whenBooksMatch_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(secondBook);
        entityManager.persist(lowStockBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByCategoryAndStockGreaterThan(PROGRAMMING, 10);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(book -> book.getCategory().equals(PROGRAMMING));
        assertThat(result).allMatch(book -> book.getStock() > 10);
    }

    @Test
    @DisplayName("findByTitleContainingIgnoreCase should return books with partial title match")
    void findByTitleContainingIgnoreCase_whenTitleMatches_shouldReturnBooks() {
        entityManager.persist(sampleBook);
        entityManager.persist(secondBook);
        entityManager.flush();

        List<Book> result = bookRepository.findByTitleContainingIgnoreCase("clean");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("findAllByOrderByCreatedAtDesc should return books ordered by creation date")
    void findAllByOrderByCreatedAtDesc_shouldReturnBooksOrderedByCreatedAt() {
        entityManager.persist(sampleBook);
        entityManager.flush();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        entityManager.persist(secondBook);
        entityManager.flush();

        List<Book> result = bookRepository.findAllByOrderByCreatedAtDesc();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCreatedAt()).isAfterOrEqualTo(result.get(1).getCreatedAt());
    }
}
