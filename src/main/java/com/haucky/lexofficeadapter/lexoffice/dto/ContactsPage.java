package com.haucky.lexofficeadapter.lexoffice.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactsPage {
    private List<Contact> content;
    private Integer totalPages;
    private Integer totalElements;
    private Boolean last;
    private Integer size;
    private Integer number;
    private Boolean first;
    private Integer numberOfElements;
}