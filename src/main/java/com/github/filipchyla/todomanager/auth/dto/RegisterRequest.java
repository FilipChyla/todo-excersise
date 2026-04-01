package com.github.filipchyla.todomanager.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name should not be blank")
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank(message = "Password should not be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{8,}$",
            message = "Password should have at least 8 characters one capital letter, one small letter, one number and" +
                    " one special character")
    private String password;
}
