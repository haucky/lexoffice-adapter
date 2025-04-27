package com.haucky.lexofficeadapter.lexoffice.dto;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class Addresses {
    @Valid
    private List<Address> billing;
    @Valid
    private List<Address> shipping;
}