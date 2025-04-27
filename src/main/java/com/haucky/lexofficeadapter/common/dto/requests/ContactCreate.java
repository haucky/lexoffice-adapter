package com.haucky.lexofficeadapter.common.dto.requests;


import com.haucky.lexofficeadapter.common.dto.*;
import com.haucky.lexofficeadapter.lexoffice.dto.Addresses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactCreate {
    @NotNull
    private Integer version = 0;
    
    @NotNull
    private Roles roles;
    
    @NotNull
    @Valid
    private Company company;

    @Valid
    private Addresses addresses;


    @Valid
    private EmailAddresses emailAddresses;

    @Valid
    private PhoneNumbers phoneNumbers;
    
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}