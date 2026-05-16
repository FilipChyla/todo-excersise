package com.github.filipchyla.todomanager.auth.service;

import com.github.filipchyla.todomanager.auth.dto.AuthenticationRequest;
import com.github.filipchyla.todomanager.auth.dto.AuthenticationResponse;
import com.github.filipchyla.todomanager.auth.dto.RegisterRequest;
import com.github.filipchyla.todomanager.shared.exception.EmailTakenException;
import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private User user;

    private final String JWT_TOKEN = "test.jwt.token";

    @BeforeEach
    void setUp() {
        String userEmail = "user@example.com";
        String userPassword = "pass";
        registerRequest = new RegisterRequest(userEmail, userPassword);

        authenticationRequest = new AuthenticationRequest(userEmail, userPassword);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(userEmail);
        user.setPassword(userPassword);
    }

    @Test
    void shouldRegisterNewUserWhenGivenValidEmailAndPassword() {
        //Arrange
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword1!");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn(JWT_TOKEN);

        //Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        //Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void shouldThrowExceptionAndNotGenerateTokenWhenGivenExistingEmail() {
        //Arrange
        when(userRepository.existsByEmail(any())).thenReturn(true);

        //Act
        assertThrows(EmailTakenException.class, () ->
                authenticationService.register(registerRequest)
        );

        //Assert
        verify(jwtService, times(0)).generateToken(any());
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void shouldAuthenticateExistingUserWhenGivenCorrectCredentials() {
        //Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(JWT_TOKEN);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        //Act
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        //Assert
        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionAndNotGenerateTokenWhenGivenBadCredentials() {
        //Arrange
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        //Act
        assertThrows(BadCredentialsException.class, () ->
                authenticationService.authenticate(authenticationRequest)
        );

        //Assert
        verify(jwtService, times(0)).generateToken(any());
    }
}
