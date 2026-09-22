package com.evoluxion.hiring_portal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalResponse {

    private Long id;

    private String fullName;

    private String email;

    private String role;

    private String approvalStatus;

    private Boolean enabled;
}