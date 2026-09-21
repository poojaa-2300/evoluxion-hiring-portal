package com.evoluxion.hiring_portal.controller;

import com.evoluxion.hiring_portal.dto.InterviewerRegistrationRequest;
import com.evoluxion.hiring_portal.service.InterviewerRegistrationService;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/interviewer")
@AllArgsConstructor
public class InterviewerController {

    private final InterviewerRegistrationService interviewerRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerInterviewer(
            @Valid @RequestBody InterviewerRegistrationRequest request) {

        String response =
                interviewerRegistrationService.registerInterviewer(request);

        return ResponseEntity.ok(response);
    }
}