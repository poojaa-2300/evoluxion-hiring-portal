package com.evoluxion.hiring_portal.service;

import com.evoluxion.hiring_portal.dto.ApprovalRequest;
import com.evoluxion.hiring_portal.dto.ApprovalResponse;
import com.evoluxion.hiring_portal.entity.ApprovalStatus;
import com.evoluxion.hiring_portal.entity.Role;
import com.evoluxion.hiring_portal.entity.UserAccount;
import com.evoluxion.hiring_portal.repository.UserAccountRepository;

import lombok.AllArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor

public class AdminApprovalService {

    private final UserAccountRepository userAccountRepository;



    
    // GET PENDING HR / INTERVIEWER ACCOUNTS
    

    public List<ApprovalResponse> getPendingApprovals() {

        return userAccountRepository
                .findAll()
                .stream()

                .filter(user ->
                        user.getApprovalStatus()
                                == ApprovalStatus.PENDING)

                .filter(user ->
                        user.getRole() == Role.HR
                        ||
                        user.getRole()
                                == Role.INTERVIEWER)

                .map(this::toResponse)

                .toList();
    }

   
    // APPROVE / REJECT
   

    @Transactional
    public ApprovalResponse processApproval(
            ApprovalRequest request) {

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase();

        UserAccount user =
                userAccountRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found"
                                ));

        /*
         * Admin can approve only HR and Interviewer.
         *
         * Candidate does NOT require Admin approval.
         *
         * Admin cannot approve another Admin.
         */
        if (user.getRole() != Role.HR
                &&
                user.getRole() != Role.INTERVIEWER) {

            throw new IllegalArgumentException(
                    "Only HR and INTERVIEWER accounts can be approved"
            );
        }

        String action =
                request.getAction()
                        .trim()
                        .toUpperCase();

        if ("APPROVE".equals(action)) {

            user.setApprovalStatus(
                    ApprovalStatus.APPROVED
            );

            user.setEnabled(true);

        } else if ("REJECT".equals(action)) {

            user.setApprovalStatus(
                    ApprovalStatus.REJECTED
            );

            user.setEnabled(false);

        } else {

            throw new IllegalArgumentException(
                    "Action must be APPROVE or REJECT"
            );
        }

        user.setUpdatedAt(
                LocalDateTime.now()
        );

        UserAccount saved =
                userAccountRepository.save(user);

        return toResponse(saved);
    }

    
    // CONVERT ENTITY TO RESPONSE
    

    private ApprovalResponse toResponse(
            UserAccount user) {

        return ApprovalResponse.builder()

                .id(user.getId())

                .fullName(
                        user.getFullName()
                )

                .email(
                        user.getEmail()
                )

                .role(
                        user.getRole().name()
                )

                .approvalStatus(
                        user.getApprovalStatus()
                                .name()
                )

                .enabled(
                        user.getEnabled()
                )

                .build();
    }
}