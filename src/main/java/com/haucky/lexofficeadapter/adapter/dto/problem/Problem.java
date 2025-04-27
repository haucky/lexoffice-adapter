package com.haucky.lexofficeadapter.adapter.dto.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.net.URI;

/**
 * Problem Details for HTTP APIs (RFC 7807) compliant error response.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {
    // TODO For later: Ideally this would point to some unique URL explaining the specific problem
    //  For now just the general documentation link is set (either adapter or Lexoffice API docs)
    private URI type;
    private String title;
    @JsonSerialize(using = HttpStatusSerializer.class)
    @JsonDeserialize(using = HttpStatusDeserializer.class)
    private HttpStatus status;
    private String detail;
    private URI instance;
}