package com.haucky.lexofficeadapter.common.dto;

import java.util.List;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class Company {
    @NotEmpty
    private String name;
    
    private String taxNumber;
    private String vatRegistrationId;
    private Boolean allowTaxFreeInvoices;
    @Valid
    private List<CompanyContactPerson> contactPersons;
}