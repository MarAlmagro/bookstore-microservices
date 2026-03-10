package com.bookstore.order.contracts;

import com.bookstore.common.dto.BookDto;
import com.bookstore.order.client.CatalogClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureStubRunner(
    stubsMode = StubRunnerProperties.StubsMode.LOCAL,
    ids = "com.bookstore:catalog-service:+:stubs:8081"
)
public class CatalogClientContractTest {

    @Autowired
    private CatalogClient catalogClient;

    @Test
    void shouldGetBookById() {
        BookDto book = catalogClient.getBookById(1L);
        
        assertThat(book).isNotNull();
        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isNotBlank();
        assertThat(book.getAuthor()).isNotBlank();
        assertThat(book.getIsbn()).isNotBlank();
        assertThat(book.getPrice()).isNotNull();
        assertThat(book.getStock()).isNotNull();
    }
}
