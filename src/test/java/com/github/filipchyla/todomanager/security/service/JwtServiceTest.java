package com.github.filipchyla.todomanager.security.service;

import com.github.filipchyla.todomanager.auth.service.JwtService;
import com.github.filipchyla.todomanager.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        String testSecret = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";
        jwtService = new JwtService(testSecret, 1000L);

        user = new User();
        user.setEmail("test@email.com");
        user.setPassword("password");
    }

    @Test
    void shouldGenerateToken() {
        //Act
        String token = jwtService.generateToken(user);

        //Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        //Arrange & Act
        String token = jwtService.generateToken(user);
        String email = jwtService.extractUsername(token);

        //Assert
        assertEquals("test@email.com", email);
    }

    @Test
    void shouldInvalidateTokenForDifferentUser() {
        //Arrange & Act
        String token = jwtService.generateToken(user);
        User differentUser = new User();
        differentUser.setEmail("different@mail.com");

        //Assert
        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        //Act & Assert
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername("invalid.token.here"));
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        //Arrange
        String token = jwtService.generateToken(user);

        Thread.sleep(1000);

        //Act & Assert
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user));
    }
}