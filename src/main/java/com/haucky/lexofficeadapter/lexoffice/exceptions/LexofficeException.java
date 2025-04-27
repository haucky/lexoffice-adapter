package com.haucky.lexofficeadapter.lexoffice.exceptions;

import com.haucky.lexofficeadapter.lexoffice.dto.LexofficeLegacyError;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Optional;

/**
 * Exception for API errors returned by Lexoffice API.
 * This exception is already tightly bound to the Http response with already defined status code.
 * The global exception handler therefore just wraps those in a problem json.
 */
@Getter
@Builder
public class LexofficeException extends RuntimeException {
    private final String title;
    private final String detail;
    private final HttpStatus status;
    private final URI type;
    private final String requestId;

    public LexofficeException(String title, String detail, HttpStatus status, URI type, String requestId) {
        super(detail);
        this.title = title;
        this.detail = detail;
        this.status = status;
        this.type = type;
        this.requestId = requestId;
    }


    public static Optional<String> extractDetailsFromLegacyError(LexofficeLegacyError errorDto) {
        if (errorDto != null && errorDto.getIssueList() != null && !errorDto.getIssueList().isEmpty()) {
            LexofficeLegacyError.Issue issue = errorDto.getIssueList().get(0);
            String source = (issue.getSource() != null ? issue.getSource() : "").replace("_", " ");
            String type = (issue.getType() != null ? issue.getType() : "").replace("_", " ");
            return Optional.of(String.format("Due to '%s' (%s)",source, type));
        }
            return Optional.empty();
        }
 }
