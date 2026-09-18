package com.evoluxion.hiring_portal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;
}