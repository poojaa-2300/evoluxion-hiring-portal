package com.evoluxion.hiring_portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationResponse {

    private String message;
    private String candidateId;
    private String email;
}