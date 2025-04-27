package com.haucky.lexofficeadapter.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.haucky.lexofficeadapter.common.dto.Company;
import com.haucky.lexofficeadapter.common.dto.EmailAddresses;
import com.haucky.lexofficeadapter.common.dto.PhoneNumbers;
import com.haucky.lexofficeadapter.common.dto.Roles;
import lombok.Data;

import java.util.UUID;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactResponse {
    private UUID id;
    private UUID organizationId;
    private Integer version;
    private Roles roles;
    private Company company;
    private AddressesResponse addresses;
    private EmailAddresses emailAddresses;
    private PhoneNumbers phoneNumbers;
    private String note;
}