package com.haucky.lexofficeadapter.adapter.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CountryCodeValidator.class)
public @interface ValidCountryCode {
    String message() default "country code not available";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}