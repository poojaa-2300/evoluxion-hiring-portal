package com.evoluxion.hiring_portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id", unique = true, nullable = false)
    private String candidateId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
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
    private String role = "CANDIDATE";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}