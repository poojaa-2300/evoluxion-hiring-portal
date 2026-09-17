package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.RegistrationRequest;
import com.evoluxion.hiring_portal.dto.RegistrationResponse;
import com.evoluxion.hiring_portal.entity.Candidate;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.entity.UserAccount;
import com.evoluxion.hiring_portal.repository.CandidateRepository;
import com.evoluxion.hiring_portal.repository.UserAccountRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegistrationService {

    private final CandidateRepository candidateRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            CandidateRepository candidateRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder) {

        this.candidateRepository = candidateRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResponse registerCandidate(
            RegistrationRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        String phone = request.getPhone()
                .trim();

        // Check duplicate email in candidates table
        if (candidateRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered");
        }

        // Check duplicate email in user accounts table
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered");
        }

        // Check duplicate phone
        if (candidateRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "Phone number is already registered");
        }

        // Check password confirmation
        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Password and confirm password do not match");
        }

        // Generate unique candidate ID
        String candidateId = generateCandidateId();

        // Encode password only once
        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword());

        // Create candidate profile
        Candidate candidate = Candidate.builder()
                .candidateId(candidateId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .phone(phone)
                .password(encodedPassword)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .highestQualification(
                        request.getHighestQualification())
                .collegeUniversity(
                        request.getCollegeUniversity())
                .graduationYear(request.getGraduationYear())
                .skills(request.getSkills())
                .role("CANDIDATE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save candidate profile
        Candidate savedCandidate =
                candidateRepository.save(candidate);

        // Create authentication account
        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.CANDIDATE)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save authentication account
        userAccountRepository.save(userAccount);

        // Return safe response
        return new RegistrationResponse(
                "Candidate registration successful",
                savedCandidate.getCandidateId(),
                savedCandidate.getEmail()
        );
    }

    private String generateCandidateId() {

        return "CAN-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}