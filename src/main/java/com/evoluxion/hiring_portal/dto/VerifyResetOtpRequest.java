package com.evoluxion.hiring_portal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VerifyResetOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @Min(value = 100000, message = "OTP must be 6 digits")
    @Max(value = 999999, message = "OTP must be 6 digits")
    private Integer otp;
}