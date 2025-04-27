package com.haucky.lexofficeadapter.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Customer {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer number;
}

