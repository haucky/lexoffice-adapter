package com.haucky.lexofficeadapter.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactsPageResponse {
    private List<ContactResponse> content;
    private Integer totalPages;
    private Integer totalElements;
    private Boolean last;
    private Integer size;
    private Integer number;
    private Boolean first;
    private Integer numberOfElements;
}