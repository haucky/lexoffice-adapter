package com.haucky.lexofficeadapter.common.dto.requests;

import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import lombok.Data;

@Data
@Validated
public class ContactFilterRequest {

    @Length(min = 3, message = "Email filter must contain at least 3 characters")
    private String email;

    @Length(min = 3, message = "Name filter must contain at least 3 characters")
    private String name;

    @Positive(message = "Contact number must be positive")
    private Integer number;

    private Boolean customer;

    private Boolean vendor;
}