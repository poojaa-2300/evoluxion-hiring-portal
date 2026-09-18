package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.OtpRequest;
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
import java.util.Random;
import java.util.UUID;

@Service
public class RegistrationService {

    private final CandidateRepository candidateRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationService(
            CandidateRepository candidateRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.candidateRepository = candidateRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // =========================================================
    // CANDIDATE REGISTRATION
    // =========================================================

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

        // Encode password
        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword());

        // Generate OTP
        int otp = generateOtp();

        // OTP expires after 5 minutes
        LocalDateTime otpExpiryTime =
                LocalDateTime.now().plusMinutes(5);

        // Create candidate
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

                .otp(otp)
                .otpExpiryTime(otpExpiryTime)
                .emailVerified(false)

                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save candidate
        Candidate savedCandidate =
                candidateRepository.save(candidate);

        // Create authentication account
        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.CANDIDATE)

                // Disabled until email verification
                .enabled(false)

                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Save authentication account
        userAccountRepository.save(userAccount);

        // Send OTP
        emailService.sendOtp(
                email,
                savedCandidate.getFirstName(),
                otp
        );

        return new RegistrationResponse(
                "Registration successful. OTP has been sent to your email.",
                savedCandidate.getCandidateId(),
                savedCandidate.getEmail()
        );
    }


    // =========================================================
    // VERIFY OTP
    // =========================================================

    @Transactional
    public String verifyOtp(OtpRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // Find candidate
        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"));

        // Check whether already verified
        if (Boolean.TRUE.equals(
                candidate.getEmailVerified())) {

            return "Email is already verified";
        }

        // Check OTP
        if (candidate.getOtp() == null ||
                !candidate.getOtp()
                        .equals(request.getOtp())) {

            throw new IllegalArgumentException(
                    "Invalid OTP");
        }

        // Check OTP expiry
        if (candidate.getOtpExpiryTime() == null ||
                LocalDateTime.now()
                        .isAfter(candidate.getOtpExpiryTime())) {

            throw new IllegalArgumentException(
                    "OTP has expired. Please request a new OTP");
        }

        // Mark email as verified
        candidate.setEmailVerified(true);

        // Remove used OTP
        candidate.setOtp(null);
        candidate.setOtpExpiryTime(null);

        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        // Enable login account
        UserAccount userAccount =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"));

        userAccount.setEnabled(true);
        userAccount.setUpdatedAt(LocalDateTime.now());

        userAccountRepository.save(userAccount);

        return "Email verified successfully. You can now login.";
    }


    // =========================================================
    // RESEND OTP
    // =========================================================

    @Transactional
    public String resendOtp(String email) {

        String normalizedEmail = email
                .trim()
                .toLowerCase();

        // Find candidate
        Candidate candidate =
                candidateRepository
                        .findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"));

        // Check whether already verified
        if (Boolean.TRUE.equals(
                candidate.getEmailVerified())) {

            return "Email is already verified";
        }

        // Generate new OTP
        int newOtp = generateOtp();

        // New expiry time
        LocalDateTime newExpiryTime =
                LocalDateTime.now().plusMinutes(5);

        candidate.setOtp(newOtp);
        candidate.setOtpExpiryTime(newExpiryTime);
        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        // Send new OTP
        emailService.sendOtp(
                normalizedEmail,
                candidate.getFirstName(),
                newOtp
        );

        return "A new OTP has been sent to your email";
    }


    // =========================================================
    // GENERATE OTP
    // =========================================================

    private int generateOtp() {

        return 100000 +
                new Random().nextInt(900000);
    }


    // =========================================================
    // GENERATE CANDIDATE ID
    // =========================================================

    private String generateCandidateId() {

        return "CAN-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}