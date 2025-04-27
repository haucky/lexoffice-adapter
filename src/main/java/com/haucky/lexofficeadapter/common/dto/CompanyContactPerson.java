package com.haucky.lexofficeadapter.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyContactPerson {
    @Size(max = 25)
    private String salutation;
    
    private String firstName;
    
    @NotNull
    private String lastName;
    
    private Boolean primary = false;
    
    @Email
    private String emailAddress;
    
    private String phoneNumber;
}