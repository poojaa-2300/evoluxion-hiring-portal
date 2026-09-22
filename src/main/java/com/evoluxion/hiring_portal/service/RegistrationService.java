package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.ForgotPasswordRequest;
import com.evoluxion.hiring_portal.dto.OtpRequest;
import com.evoluxion.hiring_portal.dto.RegistrationRequest;
import com.evoluxion.hiring_portal.dto.RegistrationResponse;
import com.evoluxion.hiring_portal.dto.ResetPasswordRequest;
import com.evoluxion.hiring_portal.dto.VerifyResetOtpRequest;
import com.evoluxion.hiring_portal.entity.ApprovalStatus;
import com.evoluxion.hiring_portal.entity.Candidate;
import com.evoluxion.hiring_portal.entity.Interviewer;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.entity.UserAccount;
import com.evoluxion.hiring_portal.repository.CandidateRepository;
import com.evoluxion.hiring_portal.repository.InterviewerRepository;
import com.evoluxion.hiring_portal.repository.UserAccountRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class RegistrationService {

    private final CandidateRepository candidateRepository;
    private final InterviewerRepository interviewerRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationService(
            CandidateRepository candidateRepository,
            InterviewerRepository interviewerRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.candidateRepository = candidateRepository;
        this.interviewerRepository = interviewerRepository;
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

        if (candidateRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "A candidate with this email already exists");
        }

        if (candidateRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException(
                    "A candidate with this phone number already exists");
        }

        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists");
        }

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Password and confirm password do not match");
        }

        int otp = generateOtp();

        Candidate candidate = new Candidate();

        candidate.setCandidateId(generateCandidateId());

        candidate.setFirstName(
                request.getFirstName().trim());

        candidate.setLastName(
                request.getLastName().trim());

        candidate.setEmail(email);

        candidate.setPhone(phone);

        candidate.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        candidate.setDateOfBirth(
                request.getDateOfBirth());

        candidate.setGender(
                request.getGender());

        candidate.setHighestQualification(
                request.getHighestQualification());

        candidate.setCollegeUniversity(
                request.getCollegeUniversity());

        candidate.setGraduationYear(
                request.getGraduationYear());

        candidate.setSkills(
                request.getSkills());

        // Candidate role is stored as String
        candidate.setRole("CANDIDATE");

        candidate.setOtp(otp);

        candidate.setOtpExpiryTime(
                LocalDateTime.now().plusMinutes(5));

        candidate.setEmailVerified(false);

        candidateRepository.save(candidate);

        UserAccount userAccount = UserAccount.builder()
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.getPassword()))
                .role(Role.CANDIDATE)
                .enabled(false)
                .approvalStatus(ApprovalStatus.APPROVED)
                .fullName(
                        request.getFirstName().trim()
                        + " "
                        + request.getLastName().trim()
                )
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userAccountRepository.save(userAccount);

        emailService.sendOtp(
                email,
                candidate.getFirstName(),
                otp);

        return new RegistrationResponse(
                candidate.getCandidateId(),
                candidate.getFirstName(),
                candidate.getLastName(),
                candidate.getEmail(),
                "Registration successful. Please verify your email using the OTP sent to your email."
        );
    }

    // =========================================================
    // CANDIDATE EMAIL OTP VERIFICATION
    // =========================================================

    @Transactional
    public String verifyOtp(OtpRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"));

        if (candidate.getEmailVerified()) {
            return "Email is already verified";
        }

        if (candidate.getOtp() == null) {
            throw new IllegalArgumentException(
                    "No OTP is available. Please request a new OTP.");
        }

        if (!candidate.getOtp()
                .equals(request.getOtp())) {

            throw new IllegalArgumentException(
                    "Invalid OTP");
        }

        if (candidate.getOtpExpiryTime() == null
                || candidate.getOtpExpiryTime()
                .isBefore(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "OTP has expired. Please request a new OTP.");
        }

        candidate.setEmailVerified(true);

        candidate.setOtp(null);

        candidate.setOtpExpiryTime(null);

        candidateRepository.save(candidate);

        UserAccount userAccount =
                userAccountRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"));

        userAccount.setEnabled(true);

        userAccount.setUpdatedAt(
                LocalDateTime.now());

        userAccountRepository.save(userAccount);

        return "Email verified successfully. Your account is now active.";
    }

    // =========================================================
    // CANDIDATE RESEND OTP
    // =========================================================

    @Transactional
    public String resendOtp(String email) {

        email = email.trim().toLowerCase();

        Candidate candidate =
                candidateRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Candidate not found"));

        if (candidate.getEmailVerified()) {
            return "Email is already verified";
        }

        int otp = generateOtp();

        candidate.setOtp(otp);

        candidate.setOtpExpiryTime(
                LocalDateTime.now().plusMinutes(5));

        candidateRepository.save(candidate);

        emailService.sendOtp(
                candidate.getEmail(),
                candidate.getFirstName(),
                otp);

        return "A new OTP has been sent to your email.";
    }

    // =========================================================
    // FORGOT PASSWORD
    // CANDIDATE + INTERVIEWER
    // =========================================================

    @Transactional
    public String forgotPassword(
            ForgotPasswordRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // -----------------------------------------------------
        // CANDIDATE
        // -----------------------------------------------------

        Optional<Candidate> candidateOptional =
                candidateRepository.findByEmail(email);

        if (candidateOptional.isPresent()) {

            Candidate candidate =
                    candidateOptional.get();

            int resetOtp = generateOtp();

            candidate.setPasswordResetOtp(resetOtp);

            candidate.setPasswordResetOtpExpiryTime(
                    LocalDateTime.now().plusMinutes(5));

            candidateRepository.save(candidate);

            emailService.sendPasswordResetOtp(
                    candidate.getEmail(),
                    candidate.getFirstName(),
                    resetOtp);

            return "Password reset OTP has been sent to your email.";
        }

        // -----------------------------------------------------
        // INTERVIEWER
        // -----------------------------------------------------

        Optional<Interviewer> interviewerOptional =
                interviewerRepository.findByEmail(email);

        if (interviewerOptional.isPresent()) {

            Interviewer interviewer =
                    interviewerOptional.get();

            int resetOtp = generateOtp();

            interviewer.setPasswordResetOtp(resetOtp);

            interviewer.setPasswordResetOtpExpiryTime(
                    LocalDateTime.now().plusMinutes(5));

            interviewerRepository.save(interviewer);

            emailService.sendPasswordResetOtp(
                    interviewer.getEmail(),
                    interviewer.getFirstName(),
                    resetOtp);

            return "Password reset OTP has been sent to your email.";
        }

        throw new IllegalArgumentException(
                "No registered account found with this email");
    }

    // =========================================================
    // VERIFY RESET OTP
    // CANDIDATE + INTERVIEWER
    // =========================================================

    @Transactional
    public String verifyResetOtp(
            VerifyResetOtpRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // -----------------------------------------------------
        // CANDIDATE
        // -----------------------------------------------------

        Optional<Candidate> candidateOptional =
                candidateRepository.findByEmail(email);

        if (candidateOptional.isPresent()) {

            Candidate candidate =
                    candidateOptional.get();

            if (candidate.getPasswordResetOtp() == null) {

                throw new IllegalArgumentException(
                        "No password reset OTP found. Please request a new OTP.");
            }

            if (!candidate.getPasswordResetOtp()
                    .equals(request.getOtp())) {

                throw new IllegalArgumentException(
                        "Invalid password reset OTP");
            }

            if (candidate.getPasswordResetOtpExpiryTime() == null
                    || candidate.getPasswordResetOtpExpiryTime()
                    .isBefore(LocalDateTime.now())) {

                throw new IllegalArgumentException(
                        "Password reset OTP has expired. Please request a new OTP.");
            }

            return "Password reset OTP verified successfully.";
        }

        // -----------------------------------------------------
        // INTERVIEWER
        // -----------------------------------------------------

        Optional<Interviewer> interviewerOptional =
                interviewerRepository.findByEmail(email);

        if (interviewerOptional.isPresent()) {

            Interviewer interviewer =
                    interviewerOptional.get();

            if (interviewer.getPasswordResetOtp() == null) {

                throw new IllegalArgumentException(
                        "No password reset OTP found. Please request a new OTP.");
            }

            if (!interviewer.getPasswordResetOtp()
                    .equals(request.getOtp())) {

                throw new IllegalArgumentException(
                        "Invalid password reset OTP");
            }

            if (interviewer.getPasswordResetOtpExpiryTime() == null
                    || interviewer.getPasswordResetOtpExpiryTime()
                    .isBefore(LocalDateTime.now())) {

                throw new IllegalArgumentException(
                        "Password reset OTP has expired. Please request a new OTP.");
            }

            return "Password reset OTP verified successfully.";
        }

        throw new IllegalArgumentException(
                "No registered account found with this email");
    }

    // =========================================================
    // RESET PASSWORD
    // CANDIDATE + INTERVIEWER
    // =========================================================

    @Transactional
    public String resetPassword(
            ResetPasswordRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Password and confirm password do not match");
        }

        // -----------------------------------------------------
        // CANDIDATE
        // -----------------------------------------------------

        Optional<Candidate> candidateOptional =
                candidateRepository.findByEmail(email);

        if (candidateOptional.isPresent()) {

            Candidate candidate =
                    candidateOptional.get();

            String encodedPassword =
                    passwordEncoder.encode(
                            request.getNewPassword());

            candidate.setPassword(encodedPassword);

            candidate.setPasswordResetOtp(null);

            candidate.setPasswordResetOtpExpiryTime(null);

            candidateRepository.save(candidate);

            UserAccount userAccount =
                    userAccountRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User account not found"));

            userAccount.setPassword(encodedPassword);

            userAccount.setUpdatedAt(
                    LocalDateTime.now());

            userAccountRepository.save(userAccount);

            return "Password reset successfully.";
        }

        // -----------------------------------------------------
        // INTERVIEWER
        // -----------------------------------------------------

        Optional<Interviewer> interviewerOptional =
                interviewerRepository.findByEmail(email);

        if (interviewerOptional.isPresent()) {

            Interviewer interviewer =
                    interviewerOptional.get();

            String encodedPassword =
                    passwordEncoder.encode(
                            request.getNewPassword());

            interviewer.setPasswordResetOtp(null);

            interviewer.setPasswordResetOtpExpiryTime(null);

            interviewerRepository.save(interviewer);

            UserAccount userAccount =
                    userAccountRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "User account not found"));

            userAccount.setPassword(encodedPassword);

            userAccount.setUpdatedAt(
                    LocalDateTime.now());

            userAccountRepository.save(userAccount);

            return "Password reset successfully.";
        }

        throw new IllegalArgumentException(
                "No registered account found with this email");
    }

    // =========================================================
    // OTP GENERATOR
    // =========================================================

    private int generateOtp() {

        return 100000 +
                new Random().nextInt(900000);
    }

    // =========================================================
    // CANDIDATE ID GENERATOR
    // =========================================================

    private String generateCandidateId() {

        return "CAN-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}