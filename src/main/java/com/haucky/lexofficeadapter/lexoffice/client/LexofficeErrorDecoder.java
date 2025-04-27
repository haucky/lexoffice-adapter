package com.haucky.lexofficeadapter.lexoffice.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haucky.lexofficeadapter.lexoffice.dto.LexofficeLegacyError;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeException;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeException.LexofficeExceptionBuilder;
import com.haucky.lexofficeadapter.lexoffice.exceptions.LexofficeValidationException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.haucky.lexofficeadapter.lexoffice.LexofficeConstants.*;

/**
 * Custom error decoder that maps upstream 4xx/5xx errors into LexofficeExceptions
 */
public class LexofficeErrorDecoder implements ErrorDecoder {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(LexofficeErrorDecoder.class);
    private final ObjectMapper mapper;
    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final String ERROR_REASON_FALLBACK = "No additional information available";

    public LexofficeErrorDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus responseStatus = HttpStatus.valueOf(response.status());

        // Continue to handle with default encoder
        if (!isErrorResponse(responseStatus)) {
            return defaultErrorDecoder.decode(methodKey, response);
        }

        // TODO From what I understood from the documentation, it is not guaranteed that there is an error body (e.g. 404)
        Optional<String> responseBodyContent = getResponseBody(response);

        if (responseStatus == HttpStatus.NOT_ACCEPTABLE) {
            return handleNotAcceptable(response, responseBodyContent);
        }

        if (responseStatus == HttpStatus.UNAUTHORIZED) {
            return handleUnauthorized(response, responseBodyContent);
        }

        if (responseStatus == HttpStatus.FORBIDDEN) {
            return handleForbidden(response, responseBodyContent);
        }

        if (responseStatus == HttpStatus.BAD_REQUEST) {
            return handleBadRequest(response, responseBodyContent);
        }

        if (responseStatus == HttpStatus.SERVICE_UNAVAILABLE) {
            return handleServiceUnavailable(response);
        }

        return handleRemainingErrorCodes(response, responseBodyContent, responseStatus);
    }

    private LexofficeException handleRemainingErrorCodes(Response response, Optional<String> errorBody, HttpStatus responseStatus) {
        String genericDetails = parseLegacyErrorResponse(errorBody)
                .flatMap(LexofficeException::extractDetailsFromLegacyError)
                .orElse(responseStatus.getReasonPhrase());

        LexofficeExceptionBuilder genericExceptionBuilder = LexofficeException.builder()
                .detail(genericDetails)
                .type(LEXOFFICE_CONTACT_DOCS)
                .requestId(getRequestId(response));

        if (responseStatus.is5xxServerError()) {
            genericExceptionBuilder.title("Bad Gateway");
            genericExceptionBuilder.status(HttpStatus.BAD_GATEWAY);
        } else if (responseStatus.is4xxClientError()) { // e.g. 404
            genericExceptionBuilder.title("Lexoffice API Error: " + responseStatus.getReasonPhrase());
            genericExceptionBuilder.status(responseStatus);
        }

        return genericExceptionBuilder.build();
    }

    private static LexofficeException handleServiceUnavailable(Response response) {
        return badGateway()
                .title("Lexoffice API temporarily unavailable")
                .detail("The service is unavailable, please try again later and checkout the status page at https://status.lexware.de/")
                .type(LEXOFFICE_STATUS_DOCS)
                .requestId(getRequestId(response))
                .build();
    }

    private LexofficeException handleBadRequest(Response response, Optional<String> errorBody) {
        return badGateway()
                .detail("The upstream service considered the request format invalid or malformed" +
                        "\nReason: " + errorBody.orElse(ERROR_REASON_FALLBACK))
                .requestId(getRequestId(response))
                .build();
    }

    private LexofficeException handleForbidden(Response response, Optional<String> errorBody) {
        return LexofficeException.builder()
                .title("Lexoffice API Authorization Failed")
                .detail("Authorization with the Lexoffice API failed. This could be due to invalid token format, " +
                        "insufficient permissions, or invalid access rights." +
                        "\nReason: " + errorBody.orElse(ERROR_REASON_FALLBACK))
                .status(HttpStatus.BAD_GATEWAY)
                .type(LEXOFFICE_CONTACT_DOCS)
                .requestId(getRequestId(response))
                .build();
    }

    private LexofficeException handleUnauthorized(Response response, Optional<String> errorBody) {
        return LexofficeException.builder()
                .title("Lexoffice API Authorization Error")
                .detail("Note: Currently, the client is responsible for refreshing the token." +
                        "\nReason: " + errorBody.orElse(ERROR_REASON_FALLBACK))
                .status(HttpStatus.UNAUTHORIZED)
                .type(LEXOFFICE_TOKEN_DOCS)
                .requestId(getRequestId(response))
                .build();
    }

    private LexofficeException handleNotAcceptable(Response response, Optional<String> errorBody) {
        Optional<LexofficeLegacyError> lexofficeLegacyError = parseLegacyErrorResponse(errorBody);

        if (lexofficeLegacyError.isPresent()) {
            return LexofficeValidationException
                    .builderFromLegacyErrors(lexofficeLegacyError.get())
                    .title("Lexoffice Validation Error")
                    .type(LEXOFFICE_CONTACT_DOCS)
                    .detail("The following validations are enforced by the lexoffice api. Refer to the documentation.")
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .requestId(getRequestId(response))
                    .build();
        } else {
            return badGateway()
                    .detail("The upstream service returned invalid or malformed error format. Retrieved status code: " + response.status())
                    .requestId(getRequestId(response)).build();
        }
    }

    private boolean isErrorResponse(HttpStatus status) {
        return status.is4xxClientError() || status.is5xxServerError();
    }


    private static String getRequestId(Response response) {
        // TODO I could not find any not-vendor specific request id here
        return response.headers().getOrDefault(LEXOFFICE_REQUEST_ID_KEY, Collections.emptyList())
                .stream()
                .findFirst()
                .orElse("unknown-request");
    }

    private static Optional<String> getResponseBody(Response response) {
        if (response.body() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(feign.Util.toString(response.body().asReader()));
        } catch (IOException e) {
            log.error("Error reading response body: {}", e);
            return Optional.empty();
        }
    }

    private static LexofficeExceptionBuilder badGateway() {
        return LexofficeException.builder()
                .title("Bad Gateway")
                .type(LEXOFFICE_CONTACT_DOCS)
                .status(HttpStatus.BAD_GATEWAY);
    }

    private Optional<LexofficeLegacyError> parseLegacyErrorResponse(Optional<String> responseBody) {
        if (responseBody.isEmpty() || responseBody.get().isEmpty()) {
            return Optional.empty();
        } else {
            // TODO Not all error formats conform to legacy error or regular error response
            //  There is also for instance 401 "{ "message": "Unauthorized" }"
            //  Those should be explicitly handled instead of catching a failed deserialization
            try {
                return Optional.of(mapper.readValue(responseBody.get(), LexofficeLegacyError.class));
            } catch (JsonProcessingException e) {
                log.error("Error parsing legacy error response body: {}", e);
                return Optional.empty();
            }
        }

    }
}