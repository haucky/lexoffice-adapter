package com.haucky.lexofficeadapter.lexoffice.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LexofficeValidationError {
    private String field;
    private String message;
}