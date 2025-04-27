package com.haucky.lexofficeadapter.adapter.dto.problem;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
/**
 * Custom deserializer for HttpStatus that converts an integer status code to HttpStatus
 */
public class HttpStatusDeserializer extends JsonDeserializer<HttpStatus> {
    @Override
    public HttpStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        try {
            int statusCode = p.getValueAsInt();
            return HttpStatus.valueOf(statusCode);
        } catch (IllegalArgumentException e) {
            throw new JsonMappingException(p, "Invalid HTTP status code: " + p.getValueAsInt(), e);
        }
    }
}