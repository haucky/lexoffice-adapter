package com.haucky.lexofficeadapter.adapter.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.adapter.dto.problem.Problem.ProblemBuilder;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationError;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationProblem;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeException;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.stream.Collectors;

import static com.haucky.lexofficeadapter.adapter.AdapterConstants.ADAPTER_ERROR_DOCS;

/**
 * Global exception handler for converting various exceptions into standardized Problem JSON responses.
 *
 * This class is part of a dual error handling architecture where:
 * 1. LexofficeErrorDecoder - Handles upstream errors from Lexoffice API and converts them to LexofficeException
 * 2. GlobalExceptionHandler - Handles all exceptions in the application and converts them to Problem JSON
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class); // Use SLF4j logger

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleAdapterValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage(), ex);

        ValidationProblem problem = ValidationProblem.builder()
                .type(ADAPTER_ERROR_DOCS)
                .title("Validation Error")
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .detail("The request contains invalid parameters")
                .instance(constructInstanceUri(request))
                .build();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = "";
            String errorMessage = error.getDefaultMessage();

            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            }

            problem.addValidationError(fieldName, errorMessage);
        });

        return toEntity(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleUnknownFieldException(HttpMessageNotReadableException ex, WebRequest request) throws HttpMessageNotReadableException {
        if (ex.getCause() instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
            String propertyName = unrecognizedPropertyException.getPropertyName();
            String className = unrecognizedPropertyException.getReferringClass().getSimpleName();
            log.error("Unknown field error: Property '{}' in class {}", propertyName, className, ex);

            ValidationProblem problem = ValidationProblem.builder()
                    .type(ADAPTER_ERROR_DOCS)
                    .title("Validation Error")
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .detail("Unknown field in request: '" + propertyName + "' in " + className)
                    .instance(constructInstanceUri(request))
                    .build();

            problem.addValidationError(propertyName, "Unknown field not defined in API contract");

            return toEntity(problem);
        }

        // Re-throw other subtypes to be handled by fallback handler
        throw ex;
    }

    @ExceptionHandler(LexofficeValidationException.class)
    public ResponseEntity<Problem> handleLexofficeValidationExceptions(LexofficeValidationException validationException, WebRequest request) {
        log.error("Lexoffice validation error: {} (Request ID: {})", validationException.getDetail(), validationException.getRequestId(), validationException);

        ValidationProblem problem = ValidationProblem.builder()
                .type(validationException.getType())
                .title(validationException.getTitle())
                .status(validationException.getStatus())
                .detail(validationException.getDetail())
                .validationErrors(validationException.getValidationErrors()
                        .stream()
                        .map(e -> new ValidationError(e.getField(), e.getMessage()))
                        .collect(Collectors.toList()))
                .instance(constructInstanceUriWithRequestId(validationException.getRequestId(), request))
                .build();

        return toEntity(problem);
    }

    @ExceptionHandler(LexofficeException.class)
    public ResponseEntity<Problem> handleLexofficeExceptions(LexofficeException lexofficeException, WebRequest request) {
        log.error("Lexoffice error: {} (Request ID: {})", lexofficeException.getDetail(), lexofficeException.getRequestId(), lexofficeException);

        Problem problem = Problem.builder()
                .type(lexofficeException.getType())
                .title(lexofficeException.getTitle())
                .status(lexofficeException.getStatus())
                .detail(lexofficeException.getDetail())
                .instance(constructInstanceUriWithRequestId(lexofficeException.getRequestId(), request))
                .build();


        return toEntity(problem);
    }

    @ExceptionHandler(AdapterException.class)
    public ResponseEntity<Problem> handleAdapterExceptions(AdapterException adapterException, WebRequest request) {
        log.error("Adapter error: {} (Upstream Request ID: {})", adapterException.getDetail(),
                adapterException.getUpstreamRequestId() != null ? adapterException.getUpstreamRequestId() : "N/A", adapterException);

        ProblemBuilder problemBuilder = Problem.builder()
                .type(ADAPTER_ERROR_DOCS)
                .title(adapterException.getTitle())
                .detail(adapterException.getDetail());

        if (adapterException instanceof CountryMappingNotFoundException) {
            problemBuilder.status(HttpStatus.BAD_GATEWAY);
        } else {
            problemBuilder.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(adapterException.getUpstreamRequestId() != null) {
            problemBuilder.instance(constructInstanceUriWithRequestId(adapterException.getUpstreamRequestId(), request));
        } else {
            problemBuilder.instance(constructInstanceUri(request));
        }

        return toEntity(problemBuilder.build());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Problem> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        log.error("Type mismatch error: Failed to convert value '{}' to required type '{}'",
                ex.getValue(), ex.getRequiredType().getSimpleName(), ex);

        ValidationProblem problem = ValidationProblem.builder()
                .type(ADAPTER_ERROR_DOCS)
                .title("Invalid Parameter")
                .status(HttpStatus.BAD_REQUEST)
                .detail("Failed to convert value '" + ex.getValue() + "' to required type '" +
                        ex.getRequiredType().getSimpleName() + "'")
                .instance(constructInstanceUri(request))
                .build();

        problem.addValidationError(ex.getName(), "Invalid format");

        return toEntity(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleAllExceptions(Exception ex, WebRequest request) {
        // Safely log the exception without assuming getCause() is non-null
        String errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        log.error("Unhandled exception: {}", errorMessage, ex);

        Problem problem = Problem.builder()
                .type(ADAPTER_ERROR_DOCS)
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .detail("An unexpected error occurred")
                .instance(constructInstanceUri(request))
                .build();

        return toEntity(problem);
    }


    private static ResponseEntity<Problem> toEntity(Problem problem) {
        return new ResponseEntity<>(problem, problem.getStatus());
    }


    private static URI constructInstanceUriWithRequestId(String requestId, WebRequest request) {
        return UriComponentsBuilder.fromUri(constructInstanceUri(request))
                .pathSegment("upstream-request", requestId)
                .build().toUri();
    }

    private static URI constructInstanceUri(WebRequest request) {
        return URI.create(request.getDescription(false).substring(4));
    }
}