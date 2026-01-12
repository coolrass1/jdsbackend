package com.skk.jdsbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String firstname;
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String lastname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "NI number is required")
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

    private Boolean hasConflictOfInterest = false;

    private String conflictOfInterestComment;

    private List<Long> assignedUserIds;

    private String referenceNumber;
}
