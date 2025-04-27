package com.haucky.lexofficeadapter.adapter.controller;

import com.haucky.lexofficeadapter.adapter.dto.ContactResponse;
import com.haucky.lexofficeadapter.adapter.dto.ContactsPageResponse;
import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationProblem;
import com.haucky.lexofficeadapter.common.dto.mapper.ContactMapperImpl;
import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.common.dto.requests.ContactCreate;
import com.haucky.lexofficeadapter.common.dto.requests.ContactFilterRequest;
import com.haucky.lexofficeadapter.common.dto.requests.ContactPageRequest;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactCreated;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import com.haucky.lexofficeadapter.lexoffice.LexofficeContactService;
import io.micrometer.core.annotation.Timed;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller that provides endpoints for managing contacts in the Lexoffice system.
 * Handles operations like creating contacts, retrieving contact details by ID, and listing
 * contacts with filtering and pagination support.
 */
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
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contact created successfully",
                    content = @Content(schema = @Schema(implementation = ContactCreated.class))
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
            @Parameter(description = "Contact information to create", required = true)
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
                    content = @Content(schema = @Schema(implementation = ContactResponse.class))
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
            @Parameter(description = "UUID of the contact to retrieve", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
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
                    content = @Content(schema = @Schema(implementation = ContactsPageResponse.class))
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
            @Parameter(description = "Pagination parameters")
            @Valid @ModelAttribute ContactPageRequest pageRequest,
            @Parameter(description = "Filter parameters for contacts")
            @Valid @ModelAttribute ContactFilterRequest filter) {
        log.info("Listing contacts with pagination: {} and filter: {}", pageRequest, filter);
        ContactsPage contactsPage = contactService.getAllContactsWithFilter(pageRequest, filter);
        ContactsPageResponse contactsPageResponse = contactMapper.contactsPageToContactsPageResponse(contactsPage);
        return ResponseEntity.ok(contactsPageResponse);
    }
}