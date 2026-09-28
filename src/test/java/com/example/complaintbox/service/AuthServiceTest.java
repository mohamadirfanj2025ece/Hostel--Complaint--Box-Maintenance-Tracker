package com.example.complaintbox.service;

import com.example.complaintbox.dto.LoginRequest;
import com.example.complaintbox.dto.LoginResponse;
import com.example.complaintbox.dto.AdminRegisterRequest;
import com.example.complaintbox.dto.RegisterRequest;
import com.example.complaintbox.dto.UserResponse;
import com.example.complaintbox.entity.User;
import com.example.complaintbox.enums.Role;
import com.example.complaintbox.exception.DuplicateEmailException;
import com.example.complaintbox.exception.InvalidCredentialsException;
import com.example.complaintbox.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_alwaysCreatesRoleUserAndEncodesPassword() {
        when(userRepository.existsByEmail("kumar@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.register(
                new RegisterRequest("Kumar", "kumar@gmail.com", "123456", "205"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.USER, response.role());
        assertEquals(Role.USER, captor.getValue().getRole());
        assertEquals("encoded-password", captor.getValue().getPassword());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("kumar@gmail.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(
                new RegisterRequest("Kumar", "kumar@gmail.com", "123456", "205")));

        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void registerAdmin_createsFirstAdminWithoutRegistrationKey() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(userRepository.existsByEmail("warden@hostel.com")).thenReturn(false);
        when(passwordEncoder.encode("A-very-strong-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.registerAdmin(
                new AdminRegisterRequest("Warden", "warden@hostel.com", "A-very-strong-password"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.ADMIN, response.role());
        assertEquals(Role.ADMIN, captor.getValue().getRole());
        assertEquals("encoded-password", captor.getValue().getPassword());
    }

    @Test
    void registerAdmin_whenAdminExists_isRejected() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        assertThrows(InvalidCredentialsException.class, () -> authService.registerAdmin(
                new AdminRegisterRequest("Warden", "warden@hostel.com", "A-very-strong-password")));

        verify(userRepository, Mockito.never()).save(any(User.class));
    }

    @Test
    void login_correctPassword_returnsRole() {
        User admin = new User();
        admin.setName("Warden");
        admin.setEmail("admin@complaintbox.com");
        admin.setPassword("hash");
        admin.setRole(Role.ADMIN);
        when(userRepository.findByEmail("admin@complaintbox.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("Admin@123", "hash")).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest("admin@complaintbox.com", "Admin@123"));

        assertEquals(Role.ADMIN, response.role());
        assertEquals("Login successful", response.message());
    }

    @Test
    void login_wrongPassword_throwsException() {
        User user = new User();
        user.setEmail("kumar@gmail.com");
        user.setPassword("hash");
        user.setRole(Role.USER);
        when(userRepository.findByEmail("kumar@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("kumar@gmail.com", "wrong")));
    }

    @Test
    void login_unknownEmail_throwsException() {
        when(userRepository.findByEmail("nobody@gmail.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("nobody@gmail.com", "123456")));
    }
}
