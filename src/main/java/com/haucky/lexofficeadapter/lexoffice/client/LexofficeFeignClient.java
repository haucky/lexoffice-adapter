package com.haucky.lexofficeadapter.lexoffice.client;

import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.common.dto.Country;
import com.haucky.lexofficeadapter.common.dto.requests.ContactCreate;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactCreated;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Feign client for interacting with the LexOffice API.
 */
@FeignClient(name = "lexoffice-api", url = "${lexoffice.api.url}", configuration = FeignClientConfig.class)
public interface LexofficeFeignClient {

    @PostMapping(value = "/contacts", consumes = "application/json")
    ContactCreated createContact(@RequestBody ContactCreate contactCreate);

    @GetMapping(value = "/contacts/{id}", produces = "application/json")
    Contact getContactById(@PathVariable("id") UUID id);

    @GetMapping(value = "/contacts", produces = "application/json")
    ContactsPage getAllContactsWithFilter(@SpringQueryMap Map<String, String> queryParams);

    @GetMapping(value = "/countries", produces = "application/json")
    ResponseEntity<List<Country>> getCountries();
}