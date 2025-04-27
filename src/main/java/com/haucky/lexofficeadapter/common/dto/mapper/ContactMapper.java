package com.haucky.lexofficeadapter.common.dto.mapper;

import com.haucky.lexofficeadapter.lexoffice.LexofficeCountryService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.haucky.lexofficeadapter.adapter.dto.ContactResponse;
import com.haucky.lexofficeadapter.adapter.dto.ContactsPageResponse;
import com.haucky.lexofficeadapter.adapter.dto.AddressesResponse;
import com.haucky.lexofficeadapter.adapter.dto.AddressResponse;
import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import com.haucky.lexofficeadapter.lexoffice.dto.Addresses;
import com.haucky.lexofficeadapter.lexoffice.dto.Address;

import java.util.List;

/**
 * Mapper to map from Lexoffice response to custom adapter response (only uni-directional)
 * Also expands country code to country name. can be discussed if this should be done in the mapper.
 */
@Mapper(componentModel = "spring", uses = {LexofficeCountryService.class})
public interface ContactMapper {

    ContactResponse contactToContactResponse(Contact contact);

    ContactsPageResponse contactsPageToContactsPageResponse(ContactsPage contactsPage);

    AddressesResponse addressesToAddressesResponse(Addresses addresses);

    @Mapping(source = "countryCode", target = "countryName", qualifiedByName = "countryCodeToName")
    AddressResponse addressToAddressResponse(Address address);

    List<AddressResponse> addressListToAddressResponseList(List<Address> addresses);
}