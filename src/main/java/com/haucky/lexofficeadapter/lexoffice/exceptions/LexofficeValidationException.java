package com.haucky.lexofficeadapter.lexoffice.exceptions;

import com.haucky.lexofficeadapter.lexoffice.dto.LexofficeLegacyError;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation specific subtype of LexofficeException.
 */
@Getter
public class LexofficeValidationException extends LexofficeException {
    private List<LexofficeValidationError> validationErrors;

    @Builder(builderMethodName = "childBuilder")
    public LexofficeValidationException(String title, String detail, HttpStatus status, URI type, String requestId, List<LexofficeValidationError> validationErrors) {
        super(title, detail, status, type, requestId);
        this.validationErrors = validationErrors != null ? validationErrors : new ArrayList<>();
    }

    public static LexofficeValidationExceptionBuilder builderFromLegacyErrors(LexofficeLegacyError errorDto) {
        return childBuilder()
                .validationErrors(extractLegacyErrors(errorDto));
    }

    private static List<LexofficeValidationError> extractLegacyErrors(LexofficeLegacyError errorDto) {
        List<LexofficeValidationError> validationErrors = new ArrayList<>();

        if (errorDto != null && errorDto.getIssueList() != null) {
            for (LexofficeLegacyError.Issue issue : errorDto.getIssueList()) {
                String source = issue.getSource() != null ? issue.getSource() : "";
                String i18nKey = issue.getI18nKey() != null ? issue.getI18nKey() : "";
                String type = issue.getType() != null ? issue.getType() : "";

                String message = i18nKey.replace("_", " ");
                if (type != null && !type.isEmpty()) {
                    message += " (" + type + ")";
                }

                validationErrors.add(new LexofficeValidationError(source, message));
            }
        }

        return validationErrors;
    }
}