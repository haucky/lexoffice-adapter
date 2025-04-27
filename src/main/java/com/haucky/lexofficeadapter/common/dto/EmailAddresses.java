package com.haucky.lexofficeadapter.common.dto;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailAddresses {
    private List<@Email String> business;
    private List<@Email String> office;
    @JsonProperty("private")
    private List<@Email String> private_;
    private List<@Email String> other;
}