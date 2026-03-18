package com.github.filipchyla.todomanager.security.service;

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
        jwtService = new JwtService(testSecret, 900L);

        user = new User();
        user.setEmail("test@email.com");
        user.setPassword("password");
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(user);
        String email = jwtService.extractUsername(token);

        assertEquals("test@email.com", email);
    }

    @Test
    void shouldInvalidateTokenForDifferentUser() {
        String token = jwtService.generateToken(user);
        User differentUser = new User();
        differentUser.setEmail("different@mail.com");

        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        assertThrows(MalformedJwtException.class, () -> jwtService.extractUsername("invalid.token.here"));
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        String token = jwtService.generateToken(user);

        Thread.sleep(1000);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user));
    }
}