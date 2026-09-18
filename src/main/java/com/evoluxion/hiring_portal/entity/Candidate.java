package com.evoluxion.hiring_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "candidates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_candidate_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_candidate_phone",
                        columnNames = "phone"
                ),
                @UniqueConstraint(
                        name = "uk_candidate_candidate_id",
                        columnNames = "candidate_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "candidate_id",
            unique = true,
            nullable = false,
            length = 20
    )
    private String candidateId;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            unique = true,
            nullable = false,
            length = 150
    )
    private String email;

    @Column(
            unique = true,
            nullable = false,
            length = 20
    )
    private String phone;

    @Column(nullable = false)
    private String password;

    private LocalDate dateOfBirth;

    @Column(length = 30)
    private String gender;

    @Column(length = 150)
    private String highestQualification;

    @Column(length = 200)
    private String collegeUniversity;

    private Integer graduationYear;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String role = "CANDIDATE";

    // OTP used for email verification
    @Column(name = "otp")
    private Integer otp;

    // OTP expiry time
    @Column(name = "otp_expiry_time")
    private LocalDateTime otpExpiryTime;

    // Email verification status
    @Column(
            name = "email_verified",
            nullable = false
    )
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (emailVerified == null) {
            emailVerified = false;
        }

        if (role == null || role.isBlank()) {
            role = "CANDIDATE";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}