package com.haucky.lexofficeadapter.adapter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller that provides API information
 */
@RestController
@RequestMapping("/")
@Tag(name = "API Info", description = "Information about the API and available resources")
// TODO Ideally this would be extracted from the OpenAPI definition (a shortened version)
public class HomeController {

    @Value("${spring.application.name:Lexoffice Adapter API}")
    private String applicationName;

    @Value("${api.version:1.0.0}")
    private String apiVersion;

    @GetMapping
    @Operation(
            summary = "Get API information",
            description = "Returns general information about the API, including version and available resources. This endpoint is publicly accessible without authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "API information retrieved successfully",
                    content = @Content(
                            schema = @Schema(
                                    implementation = Map.class,
                                    description = "Map containing API information and available resources"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = new HashMap<>();
        apiInfo.put("name", applicationName);
        apiInfo.put("version", apiVersion);
        apiInfo.put("description", "API for managing Lexoffice contacts and related operations");
        apiInfo.put("status", "operational");
        apiInfo.put("documentation", "/api.html");

        // Define available resources
        Map<String, Object> resources = new HashMap<>();

        Map<String, String> contactsResource = new HashMap<>();
        contactsResource.put("href", "/v1/contacts");
        contactsResource.put("description", "Manage Lexoffice contacts");
        resources.put("contacts", contactsResource);

        Map<String, String> cacheResource = new HashMap<>();
        cacheResource.put("href", "/v1/cache");
        cacheResource.put("description", "Cache management operations");
        resources.put("cache", cacheResource);

        apiInfo.put("resources", resources);

        return ResponseEntity.ok(apiInfo);
    }
}