package com.finance.dashboard.service;

import com.finance.dashboard.dto.request.CreateUserRequest;
import com.finance.dashboard.dto.request.UpdateUserRequest;
import com.finance.dashboard.dto.response.UserResponse;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.enums.UserStatus;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.exception.DuplicateResourceException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@finance.com")
                .password("encoded_password")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should create user when email is not taken")
    void createUser_newEmail_savesAndReturns() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Analyst");
        request.setEmail("jane@finance.com");
        request.setPassword("secret123");
        request.setRole(Role.ANALYST);

        when(userRepository.existsByEmailAndDeletedFalse(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u = User.builder()
                    .id(2L).name(u.getName()).email(u.getEmail())
                    .password(u.getPassword()).role(u.getRole()).status(u.getStatus())
                    .build();
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getEmail()).isEqualTo("jane@finance.com");
        assertThat(response.getRole()).isEqualTo(Role.ANALYST);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void createUser_duplicateEmail_throwsException() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@finance.com");
        request.setName("Duplicate");
        request.setPassword("pass123");
        request.setRole(Role.VIEWER);

        when(userRepository.existsByEmailAndDeletedFalse(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existing@finance.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user ID does not exist")
    void getUserById_notFound_throwsException() {
        when(userRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should update name and role when provided")
    void updateUser_partialUpdate_appliesChanges() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Admin Updated");
        request.setRole(Role.ANALYST);

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(adminUser.getName()).isEqualTo("Admin Updated");
        assertThat(adminUser.getRole()).isEqualTo(Role.ANALYST);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Should prevent deleting last admin account")
    void deleteUser_lastAdmin_throwsBadRequest() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndDeletedFalse(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("last admin");
    }

    @Test
    @DisplayName("Should soft-delete user when more than one admin exists")
    void deleteUser_nonLastAdmin_marksDeleted() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndDeletedFalse(Role.ADMIN)).thenReturn(2L);
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        userService.deleteUser(1L);

        assertThat(adminUser.isDeleted()).isTrue();
        assertThat(adminUser.getDeletedAt()).isNotNull();
    }
}
