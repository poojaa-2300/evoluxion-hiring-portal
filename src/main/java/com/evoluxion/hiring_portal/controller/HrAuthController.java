package com.evoluxion.hiring_portal.controller;

import com.evoluxion.hiring_portal.dto.*;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.service.StaffAuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/hr")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
public class HrAuthController {

    private final StaffAuthService staffAuthService;

 

   
    // HR REGISTRATION
   

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid
            @RequestBody HrRegistrationRequest request) {

        String response =
                staffAuthService.registerHr(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

   
    // HR LOGIN
   

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody LoginRequest request) {

        LoginResponse response =
                staffAuthService.loginHr(request);

        return ResponseEntity.ok(response);
    }

   
    // HR FORGOT PASSWORD
   

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid
            @RequestBody ForgotPasswordRequest request) {

        String response =
                staffAuthService.forgotPassword(
                        request,
                        Role.HR
                );

        return ResponseEntity.ok(response);
    }

   
    // HR VERIFY RESET OTP
 

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(
            @Valid
            @RequestBody VerifyResetOtpRequest request) {

        String response =
                staffAuthService.verifyResetOtp(
                        request,
                        Role.HR
                );

        return ResponseEntity.ok(response);
    }

 
    // HR RESET PASSWORD
   

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid
            @RequestBody ResetPasswordRequest request) {

        String response =
                staffAuthService.resetPassword(
                        request,
                        Role.HR
                );

        return ResponseEntity.ok(response);
    }
}
