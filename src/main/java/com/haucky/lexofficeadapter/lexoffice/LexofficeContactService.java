package com.haucky.lexofficeadapter.lexoffice;

import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.common.dto.requests.ContactCreate;
import com.haucky.lexofficeadapter.common.dto.requests.ContactFilterRequest;
import com.haucky.lexofficeadapter.common.dto.requests.ContactPageRequest;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactCreated;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import com.haucky.lexofficeadapter.lexoffice.client.LexofficeFeignClient;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Service for interacting with contacts, wrapping the lexoffice client
 */
@Service
public class LexofficeContactService {
    private static final Logger logger = LoggerFactory.getLogger(LexofficeContactService.class); // Use SLF4j logger

    private final LexofficeFeignClient lexofficeFeignClient;

    public LexofficeContactService(LexofficeFeignClient lexofficeFeignClient) {
        this.lexofficeFeignClient = lexofficeFeignClient;
    }

    @Timed(value = "outbound.lexoffice.contacts.create", description = "Time taken for outbound call to create contact")
    public ContactCreated createContact(ContactCreate contactCreate) {
        return lexofficeFeignClient.createContact(contactCreate);
    }

    @Timed(value = "outbound.lexoffice.contacts.getById", description = "Time taken for outbound call to get contact by ID")
    public Contact getContactById(UUID id) {
        logger.debug("Retrieving contact with ID: {}", id);
        return lexofficeFeignClient.getContactById(id);
    }

    @Timed(value = "outbound.lexoffice.contacts.getAll", description = "Time taken for outbound call to get all contacts")
    public ContactsPage getAllContactsWithFilter(ContactPageRequest contactPageRequest, ContactFilterRequest contactFilterRequest) {
        // Call the Feign client to get the contacts
        return lexofficeFeignClient.getAllContactsWithFilter(mergeQueryParams(contactPageRequest, contactFilterRequest));
    }

    // TODO: Feign client can only accept one @SpringQueryMap but I want to keep both query DTOs separate since they handle different concerns
    //  Putting them in a single hashmap is a technical concern and is abstracted away in this service.
    //  I'm not happy with this solution and probably should go with a unified DTO
    public static Map<String, String> mergeQueryParams(ContactPageRequest pageRequest, ContactFilterRequest filterRequest) {
        Map<String, String> queryParams = new HashMap<>();

        BiConsumer<String, Object> addIfNotNull = (key, value) ->
                Optional.ofNullable(value).ifPresent(v -> queryParams.put(key, v.toString()));

        addIfNotNull.accept("page", pageRequest.getPage());
        addIfNotNull.accept("size", pageRequest.getSize());
        addIfNotNull.accept("email", filterRequest.getEmail());
        addIfNotNull.accept("name", filterRequest.getName());
        addIfNotNull.accept("number", filterRequest.getNumber());
        addIfNotNull.accept("customer", filterRequest.getCustomer());
        addIfNotNull.accept("vendor", filterRequest.getVendor());

        return queryParams;
    }
}