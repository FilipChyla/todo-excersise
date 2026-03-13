package com.github.filipchyla.todomanager.auth.service;

import com.github.filipchyla.todomanager.auth.dto.AuthenticationRequest;
import com.github.filipchyla.todomanager.auth.dto.AuthenticationResponse;
import com.github.filipchyla.todomanager.auth.dto.RegisterRequest;
import com.github.filipchyla.todomanager.security.service.JwtService;
import com.github.filipchyla.todomanager.shared.exception.DuplicateUserException;
import com.github.filipchyla.todomanager.user.User;
import com.github.filipchyla.todomanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateUserException("Email is taken: " + registerRequest.getEmail());
        }
        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userRepository.save(newUser);

        String token = jwtService.generateToken(newUser);

        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(user);

        return new AuthenticationResponse(jwtToken);
    }
}
