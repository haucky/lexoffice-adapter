package com.haucky.lexofficeadapter.adapter.dto.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Problem Details for HTTP APIs (RFC 7807) compliant error response
 * specifically for validation errors.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ValidationProblem extends Problem {
    @Builder.Default
    private List<ValidationError> validationErrors = new ArrayList<>();
    public void addValidationError(String field, String message) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        validationErrors.add(new ValidationError(field, message));
    }
}