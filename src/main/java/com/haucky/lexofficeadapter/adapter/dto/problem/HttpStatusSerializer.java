package com.haucky.lexofficeadapter.adapter.dto.problem;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * Custom serializer for HttpStatus that converts it to an integer status code
 */
public class HttpStatusSerializer extends JsonSerializer<HttpStatus> {
    @Override
    public void serialize(HttpStatus value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeNumber(value.value());
        } else {
            gen.writeNull();
        }
    }
}