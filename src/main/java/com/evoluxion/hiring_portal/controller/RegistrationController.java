package com.evoluxion.hiring_portal.controller;

import com.evoluxion.hiring_portal.dto.RegistrationRequest;
import com.evoluxion.hiring_portal.dto.RegistrationResponse;
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

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request) {

        RegistrationResponse response =
                registrationService.registerCandidate(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}