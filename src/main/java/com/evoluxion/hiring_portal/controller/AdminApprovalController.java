package com.evoluxion.hiring_portal.controller;

import com.evoluxion.hiring_portal.dto.ApprovalRequest;
import com.evoluxion.hiring_portal.dto.ApprovalResponse;
import com.evoluxion.hiring_portal.service.AdminApprovalService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/approvals")
@CrossOrigin(origins = "http://localhost:5173")
@AllArgsConstructor
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;


   
    // GET PENDING APPROVALS
   

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApprovalResponse>>
    getPendingApprovals() {

        return ResponseEntity.ok(
                adminApprovalService
                        .getPendingApprovals()
        );
    }

  
    // APPROVE / REJECT HR OR INTERVIEWER
   

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApprovalResponse>
    processApproval(
            @Valid
            @RequestBody ApprovalRequest request) {

        ApprovalResponse response =
                adminApprovalService
                        .processApproval(request);

        return ResponseEntity.ok(response);
    }
}