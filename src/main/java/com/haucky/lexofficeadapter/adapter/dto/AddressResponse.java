package com.haucky.lexofficeadapter.adapter.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressResponse {
    private String supplement;
    private String street;
    private String zip;
    private String city;
    
    @NotNull
    private String countryName;
}