package com.haucky.lexofficeadapter.adapter.validations;

import com.haucky.lexofficeadapter.lexoffice.LexofficeCountryService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {
    
    private final LexofficeCountryService countryService;
    
    @Autowired
    public CountryCodeValidator(LexofficeCountryService countryService) {
        this.countryService = countryService;
    }
    
    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext context) {
        return countryService.isValidCountryCode(countryCode);
    }
}