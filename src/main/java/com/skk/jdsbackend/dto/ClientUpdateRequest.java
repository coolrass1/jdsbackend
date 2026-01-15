package com.skk.jdsbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientUpdateRequest {

    @Size(max = 100, message = "Firstname must not exceed 100 characters")
    private String firstname;

    @Size(max = 100, message = "Lastname must not exceed 100 characters")
    private String lastname;

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 20, message = "NI number must not exceed 20 characters")
    private String ni_number;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private String address;

    @Size(max = 100, message = "Company must not exceed 100 characters")
    private String company;

    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    private String occupation;

    private String additionalNote;

    private Boolean hasConflictOfInterest;

    private String conflictOfInterestComment;

    private String referenceNumber;

    private Long assignedUserId;
}
