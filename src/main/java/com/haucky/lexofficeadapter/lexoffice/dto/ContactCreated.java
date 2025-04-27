package com.haucky.lexofficeadapter.lexoffice.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactCreated {
    private UUID id;
    private String resourceUri;
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;
    private Integer version;
}