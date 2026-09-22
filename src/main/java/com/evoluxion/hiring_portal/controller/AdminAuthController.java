package com.evoluxion.hiring_portal.controller;

import com.evoluxion.hiring_portal.dto.*;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.service.StaffAuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor

public class AdminAuthController {

    private final StaffAuthService staffAuthService;

   

    
    // ADMIN LOGIN


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody LoginRequest request) {

        LoginResponse response =
                staffAuthService.loginAdmin(request);

        return ResponseEntity.ok(response);
    }

    
    // ADMIN FORGOT PASSWORD


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid
            @RequestBody ForgotPasswordRequest request) {

        String response =
                staffAuthService.forgotPassword(
                        request,
                        Role.ADMIN
                );

        return ResponseEntity.ok(response);
    }


    // ADMIN VERIFY RESET OTP


    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(
            @Valid
            @RequestBody VerifyResetOtpRequest request) {

        String response =
                staffAuthService.verifyResetOtp(
                        request,
                        Role.ADMIN
                );

        return ResponseEntity.ok(response);
    }

    
    // ADMIN RESET PASSWORD
  

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid
            @RequestBody ResetPasswordRequest request) {

        String response =
                staffAuthService.resetPassword(
                        request,
                        Role.ADMIN
                );

        return ResponseEntity.ok(response);
    }
}