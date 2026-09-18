package com.evoluxion.hiring_portal.controller;

import com.evoluxion.hiring_portal.dto.ForgotPasswordRequest;
import com.evoluxion.hiring_portal.dto.OtpRequest;
import com.evoluxion.hiring_portal.dto.RegistrationRequest;
import com.evoluxion.hiring_portal.dto.RegistrationResponse;
import com.evoluxion.hiring_portal.dto.ResetPasswordRequest;
import com.evoluxion.hiring_portal.dto.VerifyResetOtpRequest;
import com.evoluxion.hiring_portal.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(
            RegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    // =========================================================
    // CANDIDATE REGISTRATION
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request) {

        RegistrationResponse response =
                registrationService.registerCandidate(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // VERIFY REGISTRATION OTP
    // =========================================================

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody OtpRequest request) {

        String response =
                registrationService.verifyOtp(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // RESEND REGISTRATION OTP
    // =========================================================

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(
            @RequestParam String email) {

        String response =
                registrationService.resendOtp(email);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String response =
                registrationService.forgotPassword(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // VERIFY PASSWORD RESET OTP
    // =========================================================

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<String> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {

        String response =
                registrationService.verifyResetOtp(request);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String response =
                registrationService.resetPassword(request);

        return ResponseEntity.ok(response);
    }
}