package com.haucky.lexofficeadapter.lexoffice.dto;


import com.haucky.lexofficeadapter.adapter.validations.ValidCountryCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Address {
    private String supplement;
    private String street;
    private String zip;
    private String city;
    
    @NotNull
    @Pattern(regexp = "^[A-Z]{2}$")
    @ValidCountryCode
    private String countryCode;
}