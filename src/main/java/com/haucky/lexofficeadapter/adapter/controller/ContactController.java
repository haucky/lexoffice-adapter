package com.haucky.lexofficeadapter.adapter.controller;

import com.haucky.lexofficeadapter.adapter.dto.ContactResponse;
import com.haucky.lexofficeadapter.adapter.dto.ContactsPageResponse;
import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationProblem;
import com.haucky.lexofficeadapter.common.dto.mapper.ContactMapperImpl;
import com.haucky.lexofficeadapter.common.dto.requests.ContactCreate;
import com.haucky.lexofficeadapter.common.dto.requests.ContactFilterRequest;
import com.haucky.lexofficeadapter.common.dto.requests.ContactPageRequest;
import com.haucky.lexofficeadapter.lexoffice.LexofficeContactService;
import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactCreated;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/contacts")
@Validated
@Tag(name = "Contacts", description = "API for managing contacts in Lexoffice")
@SecurityRequirement(name = "bearer-jwt")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final LexofficeContactService contactService;
    private final ContactMapperImpl contactMapper;

    public ContactController(LexofficeContactService contactService, ContactMapperImpl contactMapper) {
        this.contactService = contactService;
        this.contactMapper = contactMapper;
    }

    @PostMapping
    @Operation(
            summary = "Create a new contact",
            description = "Creates a new contact in the Lexoffice system. Requires data:write scope.",
            security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contact information to create",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContactCreate.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "companyExample",
                            summary = "Sample company contact - Lexware GmbH with Scrum Master Renate Büttner",
                            value = "{\n" +
                                    "  \"version\": 0,\n" +
                                    "  \"roles\": {\n" +
                                    "    \"customer\": {\n" +
                                    "      \"number\": 10307\n" +
                                    "    }\n" +
                                    "  },\n" +
                                    "  \"company\": {\n" +
                                    "    \"name\": \"Lexware GmbH\",\n" +
                                    "    \"taxNumber\": \"DE123456789\",\n" +
                                    "    \"vatRegistrationId\": \"DE987654321\",\n" +
                                    "    \"allowTaxFreeInvoices\": false,\n" +
                                    "    \"contactPersons\": [\n" +
                                    "      {\n" +
                                    "        \"salutation\": \"Frau\",\n" +
                                    "        \"firstName\": \"Renate\",\n" +
                                    "        \"lastName\": \"Büttner\",\n" +
                                    "        \"primary\": true,\n" +
                                    "        \"emailAddress\": \"renate.buettner@haufe-lexware.net\",\n" +
                                    "        \"phoneNumber\": \"+49 221 45678901\"" +
                                    "      }\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"addresses\": {\n" +
                                    "    \"billing\": [\n" +
                                    "      {\n" +
                                    "        \"street\": \"Königsallee 27\",\n" +
                                    "        \"zip\": \"50678\",\n" +
                                    "        \"city\": \"Köln\",\n" +
                                    "        \"countryCode\": \"DE\"\n" +
                                    "      }\n" +
                                    "    ],\n" +
                                    "    \"shipping\": [\n" +
                                    "      {\n" +
                                    "        \"supplement\": \"Designstudio 3b\",\n" +
                                    "        \"street\": \"Königsallee 27\",\n" +
                                    "        \"zip\": \"50678\",\n" +
                                    "        \"city\": \"Köln\",\n" +
                                    "        \"countryCode\": \"DE\"\n" +
                                    "      }\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"emailAddresses\": {\n" +
                                    "    \"business\": [\n" +
                                    "      \"info@haufe-lexware.net\"\n" +
                                    "    ],\n" +
                                    "    \"office\": [\n" +
                                    "      \"office@haufe-lexware.net\"\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"phoneNumbers\": {\n" +
                                    "    \"business\": [\n" +
                                    "      \"+49 221 45678901\"\n" +
                                    "    ],\n" +
                                    "    \"fax\": [\n" +
                                    "      \"+49 221 45678902\"\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"note\": \"VIP Kunde - Software Unternehmen, Scrum Master Renate Büttner\"\n" +
                                    "}"
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contact created successfully",
                    content = @Content(schema = @Schema(implementation = ContactCreated.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "contactCreated",
                                    summary = "Example of a successful contact creation response",
                                    value = "{\"id\": \"86f5d7da-496a-4606-a18e-5753e19322a9\", \"resourceUri\": \"/contacts/86f5d7da-496a-4606-a18e-5753e19322a9\"}"
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request. For example when unknown fields provided",
                    content = @Content(schema = @Schema(implementation =  ValidationProblem.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid token",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient privileges (requires data:write scope)",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation error on known fields. For example caused by @NonNull but also if country code exists @ValidCountryCode, but also upstream validation exceptions which are not enforced by adapter.",
                    content = @Content(schema = @Schema(implementation = ValidationProblem.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error. For example caused by not explicitly handled exceptions",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Bad gateway - error from Lexoffice API. For example cause by 5xx from upstream, Changed format for legacy error, bad request or service unavailable.",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public ResponseEntity<ContactCreated> createContact(
            @Parameter(description = "Contact information to create", required = true,
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "companyExample",
                            summary = "Sample company contact - Köln Design GmbH",
                            value = "{\n" +
                                    "  \"version\": 0,\n" +
                                    "  \"roles\": {\n" +
                                    "    \"customer\": {\n" +
                                    "      \"number\": 10307\n" +
                                    "    }\n" +
                                    "  },\n" +
                                    "  \"company\": {\n" +
                                    "    \"name\": \"Köln Design GmbH\",\n" +
                                    "    \"taxNumber\": \"DE123456789\",\n" +
                                    "    \"vatRegistrationId\": \"DE987654321\",\n" +
                                    "    \"allowTaxFreeInvoices\": false,\n" +
                                    "    \"contactPersons\": [\n" +
                                    "      {\n" +
                                    "        \"salutation\": \"Frau\",\n" +
                                    "        \"firstName\": \"Renate\",\n" +
                                    "        \"lastName\": \"Büttner\",\n" +
                                    "        \"primary\": true,\n" +
                                    "        \"emailAddress\": \"renate.buettner@koeln-design.de\",\n" +
                                    "        \"phoneNumber\": \"+49 221 45678901\"\n" +
                                    "      }\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"addresses\": {\n" +
                                    "    \"billing\": [\n" +
                                    "      {\n" +
                                    "        \"street\": \"Königsallee 27\",\n" +
                                    "        \"zip\": \"50678\",\n" +
                                    "        \"city\": \"Köln\",\n" +
                                    "        \"countryCode\": \"DE\"\n" +
                                    "      }\n" +
                                    "    ],\n" +
                                    "    \"shipping\": [\n" +
                                    "      {\n" +
                                    "        \"supplement\": \"Designstudio 3b\",\n" +
                                    "        \"street\": \"Königsallee 27\",\n" +
                                    "        \"zip\": \"50678\",\n" +
                                    "        \"city\": \"Köln\",\n" +
                                    "        \"countryCode\": \"DE\"\n" +
                                    "      }\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"emailAddresses\": {\n" +
                                    "    \"business\": [\n" +
                                    "      \"info@koeln-design.de\"\n" +
                                    "    ],\n" +
                                    "    \"office\": [\n" +
                                    "      \"buero@koeln-design.de\"\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"phoneNumbers\": {\n" +
                                    "    \"business\": [\n" +
                                    "      \"+49 221 45678901\"\n" +
                                    "    ],\n" +
                                    "    \"fax\": [\n" +
                                    "      \"+49 221 45678902\"\n" +
                                    "    ]\n" +
                                    "  },\n" +
                                    "  \"note\": \"VIP Kunde - Designagentur\"\n" +
                                    "}"
                    ))
            @Valid @RequestBody ContactCreate contactCreate) {
        log.info("Creating new contact: {}", contactCreate);
        ContactCreated response = contactService.createContact(contactCreate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get contact by ID",
            description = "Retrieves contact details by UUID. Requires data:read scope.",
            security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contact retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ContactResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "companyResponse",
                                    summary = "Contact details for Lexware GmbH with Renate Büttner",
                                    value = "{\n" +
                                            "  \"id\": \"86f5d7da-496a-4606-a18e-5753e19322a9\",\n" +
                                            "  \"version\": 1,\n" +
                                            "  \"roles\": {\n" +
                                            "    \"customer\": {\n" +
                                            "      \"number\": 10307\n" +
                                            "    }\n" +
                                            "  },\n" +
                                            "  \"company\": {\n" +
                                            "    \"name\": \"Lexware GmbH\",\n" +
                                            "    \"taxNumber\": \"DE123456789\",\n" +
                                            "    \"vatRegistrationId\": \"DE987654321\",\n" +
                                            "    \"allowTaxFreeInvoices\": false,\n" +
                                            "    \"contactPersons\": [\n" +
                                            "      {\n" +
                                            "        \"salutation\": \"Frau\",\n" +
                                            "        \"firstName\": \"Renate\",\n" +
                                            "        \"lastName\": \"Büttner\",\n" +
                                            "        \"primary\": true,\n" +
                                            "        \"emailAddress\": \"renate.buettner@haufe-lexware.net\",\n" +
                                            "        \"phoneNumber\": \"+49 221 45678901\",\n" +
                                            "      }\n" +
                                            "    ]\n" +
                                            "  },\n" +
                                            "  \"addresses\": {\n" +
                                            "    \"billing\": [\n" +
                                            "      {\n" +
                                            "        \"street\": \"Königsallee 27\",\n" +
                                            "        \"zip\": \"50678\",\n" +
                                            "        \"city\": \"Köln\",\n" +
                                            "        \"countryCode\": \"DE\"\n" +
                                            "      }\n" +
                                            "    ],\n" +
                                            "    \"shipping\": [\n" +
                                            "      {\n" +
                                            "        \"supplement\": \"Designstudio 3b\",\n" +
                                            "        \"street\": \"Königsallee 27\",\n" +
                                            "        \"zip\": \"50678\",\n" +
                                            "        \"city\": \"Köln\",\n" +
                                            "        \"countryCode\": \"DE\"\n" +
                                            "      }\n" +
                                            "    ]\n" +
                                            "  },\n" +
                                            "  \"emailAddresses\": {\n" +
                                            "    \"business\": [\n" +
                                            "      \"info@haufe-lexware.net\"\n" +
                                            "    ],\n" +
                                            "    \"office\": [\n" +
                                            "      \"office@haufe-lexware.net\"\n" +
                                            "    ]\n" +
                                            "  },\n" +
                                            "  \"phoneNumbers\": {\n" +
                                            "    \"business\": [\n" +
                                            "      \"+49 221 45678901\"\n" +
                                            "    ],\n" +
                                            "    \"fax\": [\n" +
                                            "      \"+49 221 45678902\"\n" +
                                            "    ]\n" +
                                            "  },\n" +
                                            "  \"note\": \"VIP Kunde - Software Unternehmen, Scrum Master Renate Büttner\",\n" +
                                            "  \"archived\": false,\n" +
                                            "  \"createdDate\": \"2024-05-01T10:15:30Z\",\n" +
                                            "  \"updatedDate\": \"2024-05-03T08:45:12Z\"\n" +
                                            "}"
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid UUID format",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid token",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient privileges (requires data:read scope)",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Contact not found. Just propagated from upstream.",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public ResponseEntity<ContactResponse> getContactById(
            @Parameter(description = "UUID of the contact to retrieve", required = true,
                    example = "86f5d7da-496a-4606-a18e-5753e19322a9")
            @PathVariable UUID id) {
        log.info("Retrieving contact with ID: {}", id);
        Contact contact = contactService.getContactById(id);
        ContactResponse contactResponse = contactMapper.contactToContactResponse(contact);
        return ResponseEntity.ok(contactResponse);
    }

    @GetMapping
    @Operation(
            summary = "List contacts with filtering and pagination",
            description = "Returns a paginated list of contacts that can be filtered. Requires data:read scope.",
            security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contacts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ContactsPageResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "contactsPage",
                                    summary = "Example of paginated contacts",
                                    value = "{\n" +
                                            "  \"content\": [\n" +
                                            "    {\n" +
                                            "      \"id\": \"86f5d7da-496a-4606-a18e-5753e19322a9\",\n" +
                                            "      \"version\": 1,\n" +
                                            "      \"roles\": {\n" +
                                            "        \"customer\": {\n" +
                                            "          \"number\": 10307\n" +
                                            "        }\n" +
                                            "      },\n" +
                                            "      \"company\": {\n" +
                                            "        \"name\": \"Lexware GmbH\",\n" +
                                            "        \"taxNumber\": \"DE123456789\",\n" +
                                            "        \"vatRegistrationId\": \"DE987654321\"\n" +
                                            "      },\n" +
                                            "      \"archived\": false,\n" +
                                            "      \"createdDate\": \"2024-05-01T10:15:30Z\"\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"id\": \"a1f3b7ca-596c-4702-b28f-3753e19399b5\",\n" +
                                            "      \"version\": 2,\n" +
                                            "      \"roles\": {\n" +
                                            "        \"customer\": {\n" +
                                            "          \"number\": 10306\n" +
                                            "        }\n" +
                                            "      },\n" +
                                            "      \"company\": {\n" +
                                            "        \"name\": \"Acme Corporation\",\n" +
                                            "        \"taxNumber\": \"DE123456789\",\n" +
                                            "        \"vatRegistrationId\": \"DE987654321\"\n" +
                                            "      },\n" +
                                            "      \"archived\": false,\n" +
                                            "      \"createdDate\": \"2024-04-15T09:30:22Z\"\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"pageable\": {\n" +
                                            "    \"pageNumber\": 0,\n" +
                                            "    \"pageSize\": 5,\n" +
                                            "    \"sort\": {\n" +
                                            "      \"empty\": false,\n" +
                                            "      \"sorted\": true,\n" +
                                            "      \"unsorted\": false\n" +
                                            "    },\n" +
                                            "    \"offset\": 0,\n" +
                                            "    \"paged\": true,\n" +
                                            "    \"unpaged\": false\n" +
                                            "  },\n" +
                                            "  \"last\": false,\n" +
                                            "  \"totalElements\": 42,\n" +
                                            "  \"totalPages\": 9,\n" +
                                            "  \"size\": 5,\n" +
                                            "  \"number\": 0,\n" +
                                            "  \"sort\": {\n" +
                                            "    \"empty\": false,\n" +
                                            "    \"sorted\": true,\n" +
                                            "    \"unsorted\": false\n" +
                                            "  },\n" +
                                            "  \"first\": true,\n" +
                                            "  \"numberOfElements\": 5,\n" +
                                            "  \"empty\": false\n" +
                                            "}"
                            ))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid token",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient privileges (requires data:read scope)",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation error in paging & filter parameters",
                    content = @Content(schema = @Schema(implementation = ValidationProblem.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Bad Gateway - error from Lexoffice API. In addition to the general causes (as in POST) also caused by CountryMappingNotFoundException",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public ResponseEntity<ContactsPageResponse> getAllContactsWithFilter(
            @ParameterObject @Valid ContactPageRequest pageRequest,
            @ParameterObject @Valid ContactFilterRequest filter) {
        log.info("Listing contacts with pagination: {} and filter: {}", pageRequest, filter);
        ContactsPage contactsPage = contactService.getAllContactsWithFilter(pageRequest, filter);
        ContactsPageResponse contactsPageResponse = contactMapper.contactsPageToContactsPageResponse(contactsPage);
        return ResponseEntity.ok(contactsPageResponse);
    }
}