package com.haucky.lexofficeadapter.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.UUID;

import com.haucky.lexofficeadapter.adapter.dto.ContactsPageResponse;
import com.haucky.lexofficeadapter.common.dto.mapper.ContactMapperImpl;
import com.haucky.lexofficeadapter.lexoffice.LexofficeCountryService;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.haucky.lexofficeadapter.adapter.dto.AddressResponse;
import com.haucky.lexofficeadapter.adapter.dto.ContactResponse;
import com.haucky.lexofficeadapter.common.dto.Company;
import com.haucky.lexofficeadapter.common.dto.EmailAddresses;
import com.haucky.lexofficeadapter.common.dto.PhoneNumbers;
import com.haucky.lexofficeadapter.common.dto.Roles;
import com.haucky.lexofficeadapter.lexoffice.dto.Address;
import com.haucky.lexofficeadapter.lexoffice.dto.Addresses;
import com.haucky.lexofficeadapter.lexoffice.dto.Contact;

@ExtendWith(MockitoExtension.class)
class ContactMapperTest {

    @Mock
    private LexofficeCountryService countryService;

    @InjectMocks
    private ContactMapperImpl contactMapper;

    private Contact testContact;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        UUID testId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        testContact.setId(testId);
        testContact.setOrganizationId(orgId);
        testContact.setVersion(1);
        testContact.setNote("Test note");

        // Set up company
        Company company = new Company();
        company.setName("Test Company");
        testContact.setCompany(company);

        // Set up addresses
        Address address1 = new Address();
        address1.setSupplement("Floor 3");
        address1.setStreet("Test Street 123");
        address1.setZip("12345");
        address1.setCity("Berlin");
        address1.setCountryCode("DE");

        Address address2 = new Address();
        address2.setStreet("Second Street 456");
        address2.setZip("54321");
        address2.setCity("Munich");
        address2.setCountryCode("DE");

        Addresses addresses = new Addresses();
        addresses.setBilling(Arrays.asList(address1));
        addresses.setShipping(Arrays.asList(address2));
        testContact.setAddresses(addresses);

        // Set up other fields
        testContact.setEmailAddresses(new EmailAddresses());
        testContact.setPhoneNumbers(new PhoneNumbers());
        testContact.setRoles(new Roles());
    }

    @Test
    void givenCompleteContact_whenMappingToResponse_thenReturnCorrectContactResponse() {
        // Arrange
        when(countryService.convertCountryCodeToName("DE")).thenReturn("Germany");

        // Act
        ContactResponse result = contactMapper.contactToContactResponse(testContact);

        // Assert
        assertEquals(testContact.getId(), result.getId());
        assertEquals(testContact.getOrganizationId(), result.getOrganizationId());
        assertEquals(testContact.getVersion(), result.getVersion());
        assertEquals(testContact.getNote(), result.getNote());
        assertEquals(testContact.getCompany().getName(), result.getCompany().getName());

        assertNotNull(result.getAddresses());
        assertEquals(1, result.getAddresses().getBilling().size());
        assertEquals(1, result.getAddresses().getShipping().size());

        AddressResponse billingAddress = result.getAddresses().getBilling().get(0);
        assertEquals("Floor 3", billingAddress.getSupplement());
        assertEquals("Test Street 123", billingAddress.getStreet());
        assertEquals("12345", billingAddress.getZip());
        assertEquals("Berlin", billingAddress.getCity());
        assertEquals("Germany", billingAddress.getCountryName());

        AddressResponse shippingAddress = result.getAddresses().getShipping().get(0);
        assertEquals("Second Street 456", shippingAddress.getStreet());
        assertEquals("Munich", shippingAddress.getCity());
        assertEquals("Germany", shippingAddress.getCountryName());

        verify(countryService, times(2)).convertCountryCodeToName("DE");
    }

    @Test
    void givenAddress_whenMappingToResponse_thenReturnCorrectAddressResponse() {
        // Arrange
        Address address = new Address();
        address.setSupplement("Suite 100");
        address.setStreet("Main Street 777");
        address.setZip("90210");
        address.setCity("Los Angeles");
        address.setCountryCode("US");

        when(countryService.convertCountryCodeToName("US")).thenReturn("United States");

        // Act
        AddressResponse result = contactMapper.addressToAddressResponse(address);

        // Assert
        assertEquals("Suite 100", result.getSupplement());
        assertEquals("Main Street 777", result.getStreet());
        assertEquals("90210", result.getZip());
        assertEquals("Los Angeles", result.getCity());
        assertEquals("United States", result.getCountryName());

        verify(countryService).convertCountryCodeToName("US");
    }

    @Test
    void givenContactWithNullAddresses_whenMappingToResponse_thenHandleNullSafely() {
        // Arrange
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID());
        contact.setAddresses(null);

        // Act
        ContactResponse result = contactMapper.contactToContactResponse(contact);

        // Assert
        assertEquals(contact.getId(), result.getId());
        assertNull(result.getAddresses());
    }

    @Test
    void givenContactsPage_whenMappingToContactsPageResponse_thenReturnCorrectResponse() {
        // Arrange
        when(countryService.convertCountryCodeToName("DE")).thenReturn("Germany");

        ContactsPage contactsPage = new ContactsPage();
        contactsPage.setContent(Arrays.asList(testContact));
        contactsPage.setTotalPages(5);
        contactsPage.setTotalElements(100);
        contactsPage.setLast(false);
        contactsPage.setSize(20);
        contactsPage.setNumber(0);
        contactsPage.setFirst(true);
        contactsPage.setNumberOfElements(20);

        // Act
        ContactsPageResponse result = contactMapper.contactsPageToContactsPageResponse(contactsPage);

        // Assert
        assertEquals(contactsPage.getTotalPages(), result.getTotalPages());
        assertEquals(contactsPage.getTotalElements(), result.getTotalElements());
        assertEquals(contactsPage.getLast(), result.getLast());
        assertEquals(contactsPage.getSize(), result.getSize());
        assertEquals(contactsPage.getNumber(), result.getNumber());
        assertEquals(contactsPage.getFirst(), result.getFirst());
        assertEquals(contactsPage.getNumberOfElements(), result.getNumberOfElements());

        assertNotNull(result.getContent());
        assertEquals(1, result.getContent().size());

        ContactResponse mappedContact = result.getContent().get(0);
        assertEquals(testContact.getId(), mappedContact.getId());
        assertEquals(testContact.getOrganizationId(), mappedContact.getOrganizationId());
        assertEquals(testContact.getVersion(), mappedContact.getVersion());
        assertEquals(testContact.getNote(), mappedContact.getNote());
        assertEquals(testContact.getCompany().getName(), mappedContact.getCompany().getName());

        verify(countryService, times(2)).convertCountryCodeToName("DE");
    }

    @Test
    void givenEmptyContactsPage_whenMappingToContactsPageResponse_thenHandleEmptySafely() {
        // Arrange
        ContactsPage contactsPage = new ContactsPage();
        contactsPage.setContent(Arrays.asList());
        contactsPage.setTotalPages(0);
        contactsPage.setTotalElements(0);
        contactsPage.setLast(true);
        contactsPage.setSize(20);
        contactsPage.setNumber(0);
        contactsPage.setFirst(true);
        contactsPage.setNumberOfElements(0);

        // Act
        ContactsPageResponse result = contactMapper.contactsPageToContactsPageResponse(contactsPage);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(true, result.getLast());
        assertEquals(true, result.getFirst());
        assertNotNull(result.getContent());
        assertEquals(0, result.getContent().size());
    }
}