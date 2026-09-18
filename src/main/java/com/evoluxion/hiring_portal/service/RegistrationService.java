package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.ForgotPasswordRequest;
import com.evoluxion.hiring_portal.dto.OtpRequest;
import com.evoluxion.hiring_portal.dto.RegistrationRequest;
import com.evoluxion.hiring_portal.dto.RegistrationResponse;
import com.evoluxion.hiring_portal.dto.ResetPasswordRequest;
import com.evoluxion.hiring_portal.dto.VerifyResetOtpRequest;
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
    public RegistrationResponse registerCandidate(RegistrationRequest request) {

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        if (candidateRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        if (candidateRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "Phone number is already registered"
            );
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "Password and confirm password do not match"
            );
        }

        String candidateId = generateCandidateId();

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        int otp = generateOtp();

        LocalDateTime otpExpiryTime =
                LocalDateTime.now().plusMinutes(5);

        Candidate candidate = Candidate.builder()
                .candidateId(candidateId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .phone(phone)
                .password(encodedPassword)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .highestQualification(request.getHighestQualification())
                .collegeUniversity(request.getCollegeUniversity())
                .graduationYear(request.getGraduationYear())
                .skills(request.getSkills())
                .role("CANDIDATE")
                .otp(otp)
                .otpExpiryTime(otpExpiryTime)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Candidate savedCandidate =
                candidateRepository.save(candidate);

        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.CANDIDATE)
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userAccountRepository.save(userAccount);

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
    // VERIFY REGISTRATION OTP
    // =========================================================

    @Transactional
    public String verifyOtp(OtpRequest request) {

        String email =
                request.getEmail().trim().toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"
                                ));

        if (Boolean.TRUE.equals(candidate.getEmailVerified())) {
            return "Email is already verified";
        }

        if (candidate.getOtp() == null
                || !candidate.getOtp().equals(request.getOtp())) {

            throw new IllegalArgumentException(
                    "Invalid OTP"
            );
        }

        if (candidate.getOtpExpiryTime() == null
                || LocalDateTime.now()
                .isAfter(candidate.getOtpExpiryTime())) {

            throw new IllegalArgumentException(
                    "OTP has expired. Please request a new OTP"
            );
        }

        candidate.setEmailVerified(true);
        candidate.setOtp(null);
        candidate.setOtpExpiryTime(null);
        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        UserAccount userAccount =
                userAccountRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"
                                ));

        userAccount.setEnabled(true);
        userAccount.setUpdatedAt(LocalDateTime.now());

        userAccountRepository.save(userAccount);

        return "Email verified successfully. You can now login.";
    }

    // =========================================================
    // RESEND REGISTRATION OTP
    // =========================================================

    @Transactional
    public String resendOtp(String email) {

        String normalizedEmail =
                email.trim().toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(normalizedEmail)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"
                                ));

        if (Boolean.TRUE.equals(candidate.getEmailVerified())) {
            return "Email is already verified";
        }

        int newOtp = generateOtp();

        LocalDateTime newExpiryTime =
                LocalDateTime.now().plusMinutes(5);

        candidate.setOtp(newOtp);
        candidate.setOtpExpiryTime(newExpiryTime);
        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        emailService.sendOtp(
                normalizedEmail,
                candidate.getFirstName(),
                newOtp
        );

        return "A new OTP has been sent to your email";
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @Transactional
    public String forgotPassword(
            ForgotPasswordRequest request) {

        String email =
                request.getEmail().trim().toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"
                                ));

        int resetOtp = generateOtp();

        LocalDateTime resetOtpExpiryTime =
                LocalDateTime.now().plusMinutes(5);

        candidate.setPasswordResetOtp(resetOtp);
        candidate.setPasswordResetOtpExpiryTime(
                resetOtpExpiryTime
        );
        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        emailService.sendPasswordResetOtp(
                email,
                candidate.getFirstName(),
                resetOtp
        );

        return "Password reset OTP has been sent to your email";
    }

    // =========================================================
    // VERIFY PASSWORD RESET OTP
    // =========================================================

    @Transactional
    public String verifyResetOtp(
            VerifyResetOtpRequest request) {

        String email =
                request.getEmail().trim().toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"
                                ));

        if (candidate.getPasswordResetOtp() == null
                || !candidate.getPasswordResetOtp()
                .equals(request.getOtp())) {

            throw new IllegalArgumentException(
                    "Invalid password reset OTP"
            );
        }

        if (candidate.getPasswordResetOtpExpiryTime() == null
                || LocalDateTime.now()
                .isAfter(candidate
                        .getPasswordResetOtpExpiryTime())) {

            throw new IllegalArgumentException(
                    "Password reset OTP has expired. Please request a new OTP"
            );
        }

        return "Password reset OTP verified successfully";
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public String resetPassword(
            ResetPasswordRequest request) {

        String email =
                request.getEmail().trim().toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"
                                ));

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "New password and confirm password do not match"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        candidate.setPassword(encodedPassword);

        candidate.setPasswordResetOtp(null);
        candidate.setPasswordResetOtpExpiryTime(null);

        candidate.setUpdatedAt(LocalDateTime.now());

        candidateRepository.save(candidate);

        UserAccount userAccount =
                userAccountRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"
                                ));

        userAccount.setPassword(encodedPassword);
        userAccount.setUpdatedAt(LocalDateTime.now());

        userAccountRepository.save(userAccount);

        return "Password reset successfully. You can now login.";
    }

    // =========================================================
    // OTP GENERATION
    // =========================================================

    private int generateOtp() {

        return 100000 +
                new Random().nextInt(900000);
    }

    // =========================================================
    // CANDIDATE ID GENERATION
    // =========================================================

    private String generateCandidateId() {

        return "CAN-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}