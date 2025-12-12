package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSummaryDto {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String company;
}
