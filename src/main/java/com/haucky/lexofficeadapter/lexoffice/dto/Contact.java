package com.haucky.lexofficeadapter.lexoffice.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.haucky.lexofficeadapter.common.dto.Company;
import com.haucky.lexofficeadapter.common.dto.EmailAddresses;
import com.haucky.lexofficeadapter.common.dto.PhoneNumbers;
import com.haucky.lexofficeadapter.common.dto.Roles;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {
    private UUID id;
    private UUID organizationId;
    private Integer version;
    private Roles roles;
    private Company company;
    private Addresses addresses;
    private EmailAddresses emailAddresses;
    private PhoneNumbers phoneNumbers;
    private String note;
}