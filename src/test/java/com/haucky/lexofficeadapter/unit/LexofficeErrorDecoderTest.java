package com.haucky.lexofficeadapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeException;
import com.haucky.lexofficeadapter.lexoffice.client.LexofficeErrorDecoder;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeValidationException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.haucky.lexofficeadapter.lexoffice.LexofficeConstants.LEXOFFICE_CONTACT_DOCS;
import static com.haucky.lexofficeadapter.lexoffice.LexofficeConstants.LEXOFFICE_REQUEST_ID_KEY;
import static org.assertj.core.api.Assertions.assertThat;

public class LexofficeErrorDecoderTest {
    public static final String RESPONSE_URL = "https://api.lexoffice.io/contacts";
    LexofficeErrorDecoder errorDecoder;

    @BeforeEach
    void setUp() {
        errorDecoder = new LexofficeErrorDecoder(new ObjectMapper());
    }

    @Test
    void givenLexofficeValidationError_whenErrorDecoderDecodes_thenReturnsLexofficeValidationException() {
        // Arrange
        Response response = Response.builder()
                .status(406)
                .reason("Not Acceptable")
                .body("{\"IssueList\":[{\"i18nKey\":\"missing_entity\",\"source\":\"company.name\",\"type\":\"validation_failure\",\"additionalData\":null,\"args\":null}]}", StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.POST, RESPONSE_URL, new HashMap<>(), null, StandardCharsets.UTF_8, null))
                .build();

        // Act
        Exception exception = errorDecoder.decode("test", response);

        // Assert
        assertThat(exception).isInstanceOf(LexofficeValidationException.class);
        LexofficeValidationException validationException = (LexofficeValidationException) exception;

        assertThat(validationException.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        assertThat(validationException.getTitle()).isEqualTo("Lexoffice Validation Error");
        assertThat(validationException.getType()).isEqualTo(LEXOFFICE_CONTACT_DOCS);

        assertThat(validationException.getValidationErrors()).isNotEmpty();
        assertThat(validationException.getValidationErrors().get(0).getField()).isEqualTo("company.name");
        assertThat(validationException.getValidationErrors().get(0).getMessage()).contains("missing entity");
    }

    @Test
    void givenInvalidErrorFormat_whenErrorDecoderDecodes_thenReturnsBadGatewayException() {
        // Arrange
        Response response = Response.builder()
                .status(406)
                .reason("Not Acceptable")
                .body("This is an invalid error format", StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.POST, RESPONSE_URL, new HashMap<>(), null, StandardCharsets.UTF_8, null))
                .build();

        // Act
        Exception exception = errorDecoder.decode("test", response);

        // Assert
        assertThat(exception).isInstanceOf(LexofficeException.class);
        LexofficeException lexofficeException = (LexofficeException) exception;

        assertThat(lexofficeException.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);

        assertThat(lexofficeException.getTitle()).isEqualTo("Bad Gateway");
        assertThat(lexofficeException.getType()).isEqualTo(LEXOFFICE_CONTACT_DOCS);

        assertThat(lexofficeException.getDetail()).contains("invalid or malformed error format");
        assertThat(lexofficeException.getDetail()).contains("406");

        assertThat(lexofficeException.getRequestId()).isEqualTo("unknown-request");
    }

    @Test
    void givenUnauthorizedResponse_whenErrorDecoderDecodes_thenReturnsApiExceptionWithCorrectDetails() {
        // Arrange
        Map<String, Collection<String>> headers = new HashMap<>();
        String requestId = "test-request-id";
        headers.put(LEXOFFICE_REQUEST_ID_KEY, Collections.singletonList(requestId));

        Response response = Response.builder()
                .status(401)
                .reason("Unauthorized")
                .headers(headers)
                .body("{\"message\":\"Invalid token\"}", StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.POST, RESPONSE_URL, new HashMap<>(), null, StandardCharsets.UTF_8, null))
                .build();

        // Act
        Exception exception = errorDecoder.decode("test", response);

        // Assert
        assertThat(exception).isInstanceOf(LexofficeException.class);
        LexofficeException lexofficeException = (LexofficeException) exception;
        assertThat(lexofficeException.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(lexofficeException.getTitle()).isEqualTo("Lexoffice API Authorization Error");
        assertThat(lexofficeException.getDetail()).contains("Note: Currently, the client is responsible for refreshing the token");
        assertThat(lexofficeException.getRequestId()).isEqualTo(requestId);
    }

    @Test
    void givenServiceUnavailableResponse_whenErrorDecoderDecodes_thenReturnsApiExceptionWithBadGatewayStatus() {
        // Arrange
        LexofficeErrorDecoder errorDecoder = new LexofficeErrorDecoder(new ObjectMapper());
        Response response = Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .headers(new HashMap<>())
                .body("{\"message\":\"Service temporarily unavailable\"}", StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.POST, RESPONSE_URL, new HashMap<>(), null, StandardCharsets.UTF_8, null))
                .build();

        // Act
        Exception exception = errorDecoder.decode("test", response);

        // Assert
        assertThat(exception).isInstanceOf(LexofficeException.class);
        LexofficeException lexofficeException = (LexofficeException) exception;
        assertThat(lexofficeException.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(lexofficeException.getTitle()).isEqualTo("Lexoffice API temporarily unavailable");
        assertThat(lexofficeException.getDetail()).contains("The service is unavailable, please try again later and checkout the status page at https://status.lexware.de/");
    }

    @Test
    void givenInternalServerErrorResponse_whenErrorDecoderDecodes_thenReturnsApiExceptionWithBadGatewayStatus() {
        // Arrange
        LexofficeErrorDecoder errorDecoder = new LexofficeErrorDecoder(new ObjectMapper());
        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .headers(new HashMap<>())
                .body("{\"IssueList\":[{\"i18nKey\":\"technical_error\",\"source\":\"contact_has_not_mappable_country\",\"type\":\"technical_error\",\"additionalData\":null,\"args\":null}]}", StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.POST, RESPONSE_URL, new HashMap<>(), null, StandardCharsets.UTF_8, null))
                .build();

        // Act
        Exception exception = errorDecoder.decode("test", response);

        // Assert
        assertThat(exception).isInstanceOf(LexofficeException.class);
        LexofficeException lexofficeException = (LexofficeException) exception;

        assertThat(lexofficeException.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);

        assertThat(lexofficeException.getTitle()).isEqualTo("Bad Gateway");
        assertThat(lexofficeException.getType()).isEqualTo(LEXOFFICE_CONTACT_DOCS);

        assertThat(lexofficeException.getDetail()).contains("Due to 'contact has not mappable country' (technical error)");
    }


    @Test
    void givenForbiddenResponse_whenErrorDecoderDecodes_thenReturnsLexofficeException() {
        // Arrange
        LexofficeErrorDecoder errorDecoder = new LexofficeErrorDecoder(new ObjectMapper());
        Response response = Response.builder()
                .status(403)
                .reason("Forbidden")
                .headers(new HashMap<>())
                .body("{\"message\": \"'{accessToken}' not a valid key=value pair (missing equal-sign) in Authorization header: 'Bearer {accessToken}'.\"}", StandardCharsets.UTF_8)
                .request(Request.create(Request.HttpMethod.POST, RESPONSE_URL, new HashMap<>(), null, StandardCharsets.UTF_8, null))
                .build();

        // Act
        Exception exception = errorDecoder.decode("test", response);

        // Assert
        assertThat(exception).isInstanceOf(LexofficeException.class);
        LexofficeException lexofficeException = (LexofficeException) exception;

        assertThat(lexofficeException.getTitle()).isEqualTo("Lexoffice API Authorization Failed");
        assertThat(lexofficeException.getDetail()).contains("Authorization with the Lexoffice API failed");
        assertThat(lexofficeException.getDetail()).contains("invalid token format");
        assertThat(lexofficeException.getDetail()).contains("{\"message\": \"'{accessToken}' not a valid key=value pair (missing equal-sign) in Authorization header: 'Bearer {accessToken}'.\"}");

        assertThat(lexofficeException.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(lexofficeException.getType()).isEqualTo(LEXOFFICE_CONTACT_DOCS);
    }
}
