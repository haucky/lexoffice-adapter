package com.haucky.lexofficeadapter.adapter.dto;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class AddressesResponse {
    @Valid
    private List<AddressResponse> billing;
    @Valid
    private List<AddressResponse> shipping;
}