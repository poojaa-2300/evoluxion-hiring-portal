package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.*;
import com.evoluxion.hiring_portal.entity.ApprovalStatus;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.entity.UserAccount;
import com.evoluxion.hiring_portal.repository.UserAccountRepository;

import lombok.AllArgsConstructor;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class StaffAuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;


 
    // HR REGISTRATION
    

    @Transactional
    public String registerHr(HrRegistrationRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        String fullName =
                request.getFullName()
                        .trim();

        if (userAccountRepository.existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        if (!request.getPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException(
                    "Password and confirm password do not match"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );

        UserAccount hr = UserAccount.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.HR)

                // HR cannot login before Admin approval
                .enabled(false)

                .approvalStatus(
                        ApprovalStatus.PENDING
                )

                .fullName(fullName)

                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userAccountRepository.save(hr);

        return "HR registration submitted successfully. Waiting for Admin approval.";
    }

   
    // HR LOGIN


    public LoginResponse loginHr(
            LoginRequest request) {

        return loginStaff(
                request,
                Role.HR
        );
    }

    // =========================================================
    // ADMIN LOGIN
    // =========================================================

    public LoginResponse loginAdmin(
            LoginRequest request) {

        return loginStaff(
                request,
                Role.ADMIN
        );
    }

   
    // COMMON STAFF LOGIN
   

    private LoginResponse loginStaff(
            LoginRequest request,
            Role expectedRole) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        UserAccount user =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid email or password"
                                ));

        // Make sure HR cannot use Admin login
        if (user.getRole() != expectedRole) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        // Check approval
        if (user.getRole() == Role.HR) {

            if (user.getApprovalStatus()
                    != ApprovalStatus.APPROVED) {

                throw new IllegalArgumentException(
                        "HR account is waiting for Admin approval"
                );
            }
        }

        // Check enabled
        if (!Boolean.TRUE.equals(
                user.getEnabled())) {

            throw new IllegalArgumentException(
                    "User account is disabled"
            );
        }

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole().name()
                );

        return new LoginResponse(
                "Login successful",
                token,
                user.getEmail(),
                user.getRole().name()
        );
    }

 
    // FORGOT PASSWORD


    @Transactional
    public String forgotPassword(
            ForgotPasswordRequest request,
            Role expectedRole) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        UserAccount user =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        if (user.getRole() != expectedRole) {

            throw new IllegalArgumentException(
                    "Account not found"
            );
        }

        int otp = generateOtp();

        LocalDateTime expiry =
                LocalDateTime.now()
                        .plusMinutes(5);

        user.setPasswordResetOtp(otp);

        user.setPasswordResetOtpExpiryTime(
                expiry
        );

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        userAccountRepository.save(user);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                user.getFullName(),
                otp
        );

        return "Password reset OTP has been sent to your email";
    }

  
    // VERIFY RESET OTP
   

    @Transactional
    public String verifyResetOtp(
            VerifyResetOtpRequest request,
            Role expectedRole) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        UserAccount user =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        if (user.getRole() != expectedRole) {

            throw new IllegalArgumentException(
                    "Account not found"
            );
        }

        if (user.getPasswordResetOtp() == null
                || !user.getPasswordResetOtp()
                .equals(request.getOtp())) {

            throw new IllegalArgumentException(
                    "Invalid password reset OTP"
            );
        }

        if (user.getPasswordResetOtpExpiryTime()
                == null
                || LocalDateTime.now()
                .isAfter(
                        user.getPasswordResetOtpExpiryTime()
                )) {

            throw new IllegalArgumentException(
                    "Password reset OTP has expired"
            );
        }

        return "Password reset OTP verified successfully";
    }

  
    // RESET PASSWORD
  

    @Transactional
    public String resetPassword(
            ResetPasswordRequest request,
            Role expectedRole) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        UserAccount user =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                ));

        if (user.getRole() != expectedRole) {

            throw new IllegalArgumentException(
                    "Account not found"
            );
        }

        if (!request.getNewPassword()
                .equals(
                        request.getConfirmPassword()
                )) {

            throw new IllegalArgumentException(
                    "New password and confirm password do not match"
            );
        }

        
//          Verify OTP again here.
      
        if (user.getPasswordResetOtp() == null) {

            throw new IllegalArgumentException(
                    "Please verify the password reset OTP first"
            );
        }

        if (user.getPasswordResetOtpExpiryTime()
                == null
                || LocalDateTime.now()
                .isAfter(
                        user.getPasswordResetOtpExpiryTime()
                )) {

            throw new IllegalArgumentException(
                    "Password reset OTP has expired"
            );
        }

        String encodedPassword =
                passwordEncoder.encode(
                        request.getNewPassword()
                );

        user.setPassword(encodedPassword);

        user.setPasswordResetOtp(null);

        user.setPasswordResetOtpExpiryTime(null);

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        userAccountRepository.save(user);

        return "Password reset successfully. You can now login.";
    }


    // OTP GENERATION
   

    private int generateOtp() {

        return 100000 +
                new Random()
                        .nextInt(900000);
    }
}
