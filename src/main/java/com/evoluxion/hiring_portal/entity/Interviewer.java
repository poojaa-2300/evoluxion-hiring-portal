package com.evoluxion.hiring_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "interviewers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_interviewer_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_interviewer_phone",
                        columnNames = "phone"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // =========================================================
    // PASSWORD RESET
    // =========================================================

    @Column
    private Integer passwordResetOtp;

    @Column
    private LocalDateTime passwordResetOtpExpiryTime;

    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // =========================================================
    // JPA CALLBACKS
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}