package com.haucky.lexofficeadapter.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PhoneNumbers {
    private List<String> business;
    private List<String> office;
    private List<String> mobile;
    @JsonProperty("private")
    private List<String> private_;  // Using underscore to avoid Java keyword
    private List<String> fax;
    private List<String> other;
}