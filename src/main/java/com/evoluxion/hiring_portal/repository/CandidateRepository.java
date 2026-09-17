package com.evoluxion.hiring_portal.repository;

import com.evoluxion.hiring_portal.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}