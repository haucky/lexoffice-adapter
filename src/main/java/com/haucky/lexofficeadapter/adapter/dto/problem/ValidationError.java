package com.haucky.lexofficeadapter.adapter.dto.problem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single validation error for a specific field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}