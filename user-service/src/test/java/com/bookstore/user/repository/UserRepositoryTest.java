package com.bookstore.user.repository;

import com.bookstore.common.constants.UserRole;
import com.bookstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ContextConfiguration(classes = UserRepositoryTestConfig.class)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    private static final String TEST_EMAIL = "john.doe@example.com";
    private static final String NONEXISTENT_EMAIL = "nonexistent@example.com";
    private static final String UPPERCASE_TEST_EMAIL = "JOHN.DOE@EXAMPLE.COM";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email(TEST_EMAIL)
                .password("hashedPassword123")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("findByEmail should return user when email exists")
    void findByEmail_whenEmailExists_shouldReturnUser() {
        entityManager.persistAndFlush(testUser);

        Optional<User> result = userRepository.findByEmail(TEST_EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("findByEmail should return empty when email does not exist")
    void findByEmail_whenEmailDoesNotExist_shouldReturnEmpty() {
        Optional<User> result = userRepository.findByEmail(NONEXISTENT_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail should be case sensitive")
    void findByEmail_shouldBeCaseSensitive() {
        entityManager.persistAndFlush(testUser);

        Optional<User> resultLowerCase = userRepository.findByEmail(TEST_EMAIL);
        Optional<User> resultUpperCase = userRepository.findByEmail(UPPERCASE_TEST_EMAIL);

        assertThat(resultLowerCase).isPresent();
        assertThat(resultUpperCase).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail should return true when email exists")
    void existsByEmail_whenEmailExists_shouldReturnTrue() {
        entityManager.persistAndFlush(testUser);

        Boolean exists = userRepository.existsByEmail(TEST_EMAIL);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail should return false when email does not exist")
    void existsByEmail_whenEmailDoesNotExist_shouldReturnFalse() {
        Boolean exists = userRepository.existsByEmail(NONEXISTENT_EMAIL);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmail should be case sensitive")
    void existsByEmail_shouldBeCaseSensitive() {
        entityManager.persistAndFlush(testUser);

        Boolean existsLowerCase = userRepository.existsByEmail(TEST_EMAIL);
        Boolean existsUpperCase = userRepository.existsByEmail(UPPERCASE_TEST_EMAIL);

        assertThat(existsLowerCase).isTrue();
        assertThat(existsUpperCase).isFalse();
    }
}
