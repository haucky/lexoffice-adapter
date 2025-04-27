package com.haucky.lexofficeadapter.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Vendor {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer number;
}