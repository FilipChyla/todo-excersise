package com.github.filipchyla.todomanager.auth.controller;

import com.github.filipchyla.todomanager.auth.dto.AuthenticationRequest;
import com.github.filipchyla.todomanager.auth.dto.AuthenticationResponse;
import com.github.filipchyla.todomanager.auth.dto.RegisterRequest;
import com.github.filipchyla.todomanager.auth.service.AuthenticationService;
import com.github.filipchyla.todomanager.auth.service.JwtService;
import com.github.filipchyla.todomanager.shared.exception.EmailTakenException;
import com.github.filipchyla.todomanager.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserRepository userRepository;

    private static final String VALID_EMAIL = "newuser@example.com";
    private static final String VALID_PASSWORD = "Password123!";
    private static final String INVALID_EMAIL = "newuser.com";
    private static final String INVALID_PASSWORD = "password";

    @Test
    void shouldRegisterUserWhenGivenCorrectCredentials() throws Exception {
        //Arrange
        RegisterRequest request = new RegisterRequest(VALID_EMAIL, VALID_PASSWORD);
        AuthenticationResponse response = new AuthenticationResponse("jwt-token");
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        //Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldReturnErrorWhenCredentialsAreWrong() throws Exception {
        //Arrange
        RegisterRequest request = new RegisterRequest(INVALID_EMAIL,INVALID_PASSWORD);

        //Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message",containsString("email: Email should be valid")))
                        .andExpect(jsonPath("$.message",containsString("password: Password should have at least 8 characters one capital letter, one small letter, one number and one special character")));
    }

    @Test
    void shouldReturnErrorWhenEmailIsTaken() throws Exception {
        //Arrange
        RegisterRequest request = new RegisterRequest(VALID_EMAIL,VALID_PASSWORD);
        when(authenticationService.register(any(RegisterRequest.class))).thenThrow(new EmailTakenException("Email is taken: " + request.getEmail()));

        //Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value("Email is taken: " + request.getEmail()));
    }

    @Test
    void shouldAuthenticateUserWhenGivenCorrectCredentials() throws Exception {
        //Arrange
        AuthenticationRequest request = new AuthenticationRequest(VALID_EMAIL, VALID_PASSWORD);
        AuthenticationResponse response = new AuthenticationResponse("jwt-token");
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        //Act & Assert
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldGetErrorWhenCredentialsAreIncorrect() throws Exception {
        //Arrange
        AuthenticationRequest request = new AuthenticationRequest(VALID_EMAIL, INVALID_PASSWORD);
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenThrow(new BadCredentialsException("Incorrect credentials"));

        //Act & Assert
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message").value("Incorrect credentials"));
    }
}
