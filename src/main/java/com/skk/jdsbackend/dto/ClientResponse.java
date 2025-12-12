package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String address;
    private String company;
    private Integer casesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
