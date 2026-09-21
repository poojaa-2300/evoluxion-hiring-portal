package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.InterviewerRegistrationRequest;
import com.evoluxion.hiring_portal.entity.Interviewer;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.entity.UserAccount;
import com.evoluxion.hiring_portal.repository.InterviewerRepository;
import com.evoluxion.hiring_portal.repository.UserAccountRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InterviewerRegistrationService {

    private final InterviewerRepository interviewerRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public InterviewerRegistrationService(
            InterviewerRepository interviewerRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {

        this.interviewerRepository = interviewerRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String registerInterviewer(
            InterviewerRegistrationRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String phone = request.getPhone().trim();

        // Check duplicate interviewer email
        if (interviewerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An interviewer with this email already exists");
        }

        // Check duplicate interviewer phone
        if (interviewerRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "An interviewer with this phone number already exists");
        }

        // Check whether email is already used by another account
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        // Check password confirmation
        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Password and confirm password do not match");
        }

        // Create interviewer profile
        Interviewer interviewer = Interviewer.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .phone(phone)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create login account
        // enabled = false means Admin approval is required
        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()))
                .role(Role.INTERVIEWER)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        interviewerRepository.save(interviewer);
        userAccountRepository.save(userAccount);

        return "Interviewer registration successful. "
                + "Your account is waiting for Admin approval.";
    }
}