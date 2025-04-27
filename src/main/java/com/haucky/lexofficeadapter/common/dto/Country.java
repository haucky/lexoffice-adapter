package com.haucky.lexofficeadapter.common.dto;


import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Country {
    @Pattern(regexp = "^[A-Z]{2}$")
    private String countryCode;
    
    private String countryNameEN;
    private String countryNameDE;
    
    private TaxClassification taxClassification;
    
    public enum TaxClassification {
        de, intraCommunity, thirdPartyCountry
    }
}