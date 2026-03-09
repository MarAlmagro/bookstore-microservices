package com.bookstore.user.mapper;

import com.bookstore.common.constants.UserRole;
import com.bookstore.common.dto.UserDto;
import com.bookstore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword123")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("CUSTOMER")
                .build();
    }

    @Test
    @DisplayName("toDto should map User to UserDto correctly")
    void toDto_withValidUser_shouldMapCorrectly() {
        UserDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(result.getRole()).isEqualTo(testUser.getRole().name());
    }

    @Test
    @DisplayName("toDto should exclude password field")
    void toDto_withValidUser_shouldExcludePassword() {
        UserDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.toString()).doesNotContain("password");
        assertThat(result.toString()).doesNotContain("hashedPassword123");
    }

    @Test
    @DisplayName("toDto should return null when User is null")
    void toDto_withNullUser_shouldReturnNull() {
        UserDto result = userMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDto should handle null role")
    void toDto_withNullRole_shouldMapRoleAsNull() {
        testUser.setRole(null);

        UserDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isNull();
    }

    @Test
    @DisplayName("toEntity should map UserDto to User correctly")
    void toEntity_withValidDto_shouldMapCorrectly() {
        User result = userMapper.toEntity(testUserDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserDto.getId());
        assertThat(result.getEmail()).isEqualTo(testUserDto.getEmail());
        assertThat(result.getFirstName()).isEqualTo(testUserDto.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testUserDto.getLastName());
        assertThat(result.getRole()).isEqualTo(UserRole.valueOf(testUserDto.getRole()));
    }

    @Test
    @DisplayName("toEntity should return null when UserDto is null")
    void toEntity_withNullDto_shouldReturnNull() {
        User result = userMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntity should handle null role")
    void toEntity_withNullRole_shouldMapRoleAsNull() {
        testUserDto.setRole(null);

        User result = userMapper.toEntity(testUserDto);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isNull();
    }

    @Test
    @DisplayName("toEntity should convert ADMIN role correctly")
    void toEntity_withAdminRole_shouldConvertCorrectly() {
        testUserDto.setRole("ADMIN");

        User result = userMapper.toEntity(testUserDto);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("toEntity should convert CUSTOMER role correctly")
    void toEntity_withCustomerRole_shouldConvertCorrectly() {
        testUserDto.setRole("CUSTOMER");

        User result = userMapper.toEntity(testUserDto);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    @DisplayName("toDto should convert ADMIN role to string correctly")
    void toDto_withAdminRole_shouldConvertToString() {
        testUser.setRole(UserRole.ADMIN);

        UserDto result = userMapper.toDto(testUser);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("toDto and toEntity should be reversible for CUSTOMER role")
    void toDtoAndToEntity_withCustomerRole_shouldBeReversible() {
        UserDto dto = userMapper.toDto(testUser);
        User entity = userMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(testUser.getId());
        assertThat(entity.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(entity.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(entity.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(entity.getRole()).isEqualTo(testUser.getRole());
    }

    @Test
    @DisplayName("toDto and toEntity should be reversible for ADMIN role")
    void toDtoAndToEntity_withAdminRole_shouldBeReversible() {
        testUser.setRole(UserRole.ADMIN);

        UserDto dto = userMapper.toDto(testUser);
        User entity = userMapper.toEntity(dto);

        assertThat(entity.getRole()).isEqualTo(UserRole.ADMIN);
    }
}
