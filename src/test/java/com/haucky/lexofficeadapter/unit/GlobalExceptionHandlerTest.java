package com.haucky.lexofficeadapter.unit;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.haucky.lexofficeadapter.adapter.exception.GlobalExceptionHandler;
import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationError;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationProblem;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeException;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeValidationError;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

import static com.haucky.lexofficeadapter.adapter.AdapterConstants.ADAPTER_ERROR_DOCS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        // Using 'lenient' can be dangerous, but my use-case is even recommended in their docs: https://javadoc.io/doc/org.mockito/mockito-core/2.22.0/org/mockito/Mockito.html#lenient--
        lenient().when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void givenRequestWithInvalidData_whenValidatingRequest_thenReturnAdapterValidationProblem() {
        // Arrange
        MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("objectName", "company.name", "Company name is required");
        // Just kidding 🤭
        FieldError fieldError2 = new FieldError("objectName", "company.contactPersons[0].emailAddress",
                "Invalid email format: must not contain sevdesk");

        when(mockException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<Problem> response = exceptionHandler.handleAdapterValidationExceptions(mockException, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isInstanceOf(ValidationProblem.class);

        ValidationProblem problem = (ValidationProblem) response.getBody();
        assertThat(problem.getTitle()).isEqualTo("Validation Error");
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(problem.getDetail()).isEqualTo("The request contains invalid parameters");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/test"));

        List<ValidationError> errors = problem.getValidationErrors();
        assertThat(errors).hasSize(2);
        assertThat(errors).extracting("field").containsExactlyInAnyOrder("company.name", "company.contactPersons[0].emailAddress");
        assertThat(errors).extracting("message").containsExactlyInAnyOrder("Company name is required",
                "Invalid email format: must not contain sevdesk");
    }

    @Test
    void givenRequestWithUnknownField_whenParsingJson_thenReturnValidationProblem() {
        // Arrange
        UnrecognizedPropertyException propEx = mock(UnrecognizedPropertyException.class);
        when(propEx.getPropertyName()).thenReturn("unknownField");
        when(propEx.getReferringClass()).thenReturn((Class)Object.class);

        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(propEx);

        // Act
        ResponseEntity<Problem> response = exceptionHandler.handleUnknownFieldException(ex, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ValidationProblem.class);

        ValidationProblem problem = (ValidationProblem) response.getBody();
        assertThat(problem.getTitle()).isEqualTo("Validation Error");
        assertThat(problem.getDetail()).contains("Unknown field in request: 'unknownField'");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/test"));

        List<ValidationError> errors = problem.getValidationErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getField()).isEqualTo("unknownField");
        assertThat(errors.get(0).getMessage()).isEqualTo("Unknown field not defined in API contract");
    }

    @Test
    void givenMessageNotReadableWithNonPropertyCause_whenHandlingException_thenRethrowOriginalException() {
        // Arrange
        Exception otherCause = new RuntimeException("Some other cause");

        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(otherCause);

        // Act & Assert
        try {
            exceptionHandler.handleUnknownFieldException(ex, webRequest);
        } catch (Exception caught) {
            assertThat(caught).isSameAs(ex);
        }
    }

    @Test
    void givenLexofficeValidationException_whenHandlingException_thenReturnValidationProblem() {
        // Arrange
        URI type = URI.create("https://example.com/validation");
        String title = "Lexoffice Validation Error";
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String detail = "The request was rejected by Lexoffice due to validation errors";
        String requestId = "lex-123456";

        List<LexofficeValidationError> validationErrors = List.of(
                new LexofficeValidationError("company.name", "Company name is required"),
                new LexofficeValidationError("notes", "Note must not exceed 1000 characters")
        );

        LexofficeValidationException ex = mock(LexofficeValidationException.class);
        when(ex.getType()).thenReturn(type);
        when(ex.getTitle()).thenReturn(title);
        when(ex.getStatus()).thenReturn(status);
        when(ex.getDetail()).thenReturn(detail);
        when(ex.getRequestId()).thenReturn(requestId);
        when(ex.getValidationErrors()).thenReturn(validationErrors);

        // Act
        ResponseEntity<Problem> response = exceptionHandler.handleLexofficeValidationExceptions(ex, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(ValidationProblem.class);

        ValidationProblem problem = (ValidationProblem) response.getBody();
        assertThat(problem.getType()).isEqualTo(type);
        assertThat(problem.getTitle()).isEqualTo(title);
        assertThat(problem.getStatus()).isEqualTo(status);
        assertThat(problem.getDetail()).isEqualTo(detail);
        assertThat(problem.getInstance().toString()).contains("/api/test/upstream-request/" + requestId);

        List<ValidationError> errors = problem.getValidationErrors();
        assertThat(errors).hasSize(2);
        assertThat(errors).extracting("field").containsExactlyInAnyOrder("company.name", "notes");
        assertThat(errors).extracting("message").containsExactlyInAnyOrder("Company name is required", "Note must not exceed 1000 characters");
    }

    @Test
    void givenLexofficeException_whenHandlingException_thenReturnProblem() {
        // Arrange
        URI type = URI.create("https://example.com/error");
        String title = "Lexoffice API Error";
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String detail = "An error occurred while processing the request";
        String requestId = "lex-987654";

        LexofficeException ex = mock(LexofficeException.class);
        when(ex.getType()).thenReturn(type);
        when(ex.getTitle()).thenReturn(title);
        when(ex.getStatus()).thenReturn(status);
        when(ex.getDetail()).thenReturn(detail);
        when(ex.getRequestId()).thenReturn(requestId);

        // Act
        ResponseEntity<Problem> response = exceptionHandler.handleLexofficeExceptions(ex, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(status);

        Problem problem = response.getBody();
        assertThat(problem.getType()).isEqualTo(type);
        assertThat(problem.getTitle()).isEqualTo(title);
        assertThat(problem.getStatus()).isEqualTo(status);
        assertThat(problem.getDetail()).isEqualTo(detail);
        assertThat(problem.getInstance().toString()).contains("/api/test/upstream-request/" + requestId);
    }

    @Test
    void givenUnexpectedRuntimeException_whenHandlingException_thenReturnGenericProblem() {
        // Arrange
        Exception ex = new RuntimeException("Something went wrong", new IllegalArgumentException("Invalid argument"));

        // Act
        ResponseEntity<Problem> response = exceptionHandler.handleAllExceptions(ex, webRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Problem problem = response.getBody();
        assertThat(problem.getType()).isEqualTo(ADAPTER_ERROR_DOCS);
        assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/test"));
    }
}