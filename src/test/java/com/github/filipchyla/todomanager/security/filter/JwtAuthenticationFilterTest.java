package com.github.filipchyla.todomanager.security.filter;

import com.github.filipchyla.todomanager.auth.service.JwtService;
import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private final String JWT_TOKEN = "test.jwt.token";
    private final String USER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = spy(new MockFilterChain());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void createAndMockUser(boolean isValid) {
        User user = new User();
        user.setEmail(USER_EMAIL);

        when(jwtService.extractUsername(JWT_TOKEN)).thenReturn(USER_EMAIL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(JWT_TOKEN, user)).thenReturn(isValid);
    }


    @Test
    void givenValidToken_whenDoFilter_thenAuthenticateUser() throws ServletException, IOException {
        //Arrange
        createAndMockUser(true);
        request.addHeader("Authorization", "Bearer " + JWT_TOKEN);

        //Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        //Assert
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(USER_EMAIL, auth.getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void givenNoAuthorizationHeader_whenDoFilter_thenDoNotAuthenticate() throws ServletException, IOException {
        //Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        //Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void givenInvalidToken_whenDoFilter_thenDoNotAuthenticate() throws ServletException, IOException {
        //Arrange
        createAndMockUser(false);
        request.addHeader("Authorization", "Bearer " + JWT_TOKEN);

        //Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        //Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}