package com.github.filipchyla.todomanager.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @NotBlank(message = "Email should not be blank")
    private String email;
    @NotBlank(message = "Password should not be blank")
    private String password;
}
