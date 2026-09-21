package com.evoluxion.hiring_portal.repository;

import com.evoluxion.hiring_portal.entity.Interviewer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {

    Optional<Interviewer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}