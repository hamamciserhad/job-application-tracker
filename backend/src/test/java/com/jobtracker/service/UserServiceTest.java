package com.jobtracker.service;

import com.jobtracker.dto.request.RegisterRequest;
import com.jobtracker.entity.User;
import com.jobtracker.exception.EmailAlreadyExistsException;
import com.jobtracker.exception.InvalidCredentialsException;
import com.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("Test User")
                .password("hashed_password")
                .build();
    }

    @Test
    void register_success_savesAndReturnsUser() {
        RegisterRequest request = new RegisterRequest("new@test.com", "pass123", "New User");
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getPassword()).isEqualTo("hashed_pass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        RegisterRequest request = new RegisterRequest("test@test.com", "pass", "Test");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("rawPass", "hashed_password")).thenReturn(true);

        User result = userService.authenticate("test@test.com", "rawPass");

        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void authenticate_unknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate("unknown@test.com", "pass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void authenticate_wrongPassword_throwsInvalidCredentials() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "hashed_password")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticate("test@test.com", "wrongPass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void findByEmail_found_returnsPresent() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByEmail("test@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void findByEmail_notFound_returnsEmpty() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Optional<User> result = userService.findByEmail("nobody@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findById_found_returnsPresent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
}
