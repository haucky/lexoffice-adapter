package com.haucky.lexofficeadapter.adapter.controller;

import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.lexoffice.LexofficeCountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle cache management operations.
 * Provides endpoint for invalidating contacts cache
 */
@RestController
@RequestMapping("/v1/cache")
@Tag(name = "Cache Management", description = "Operations for managing application caches")
@SecurityRequirement(name = "bearer-jwt")
public class CacheController {

    private final LexofficeCountryService countryService;

    public CacheController(LexofficeCountryService countryService) {
        this.countryService = countryService;
    }

    @PostMapping("/countries/invalidate")
    @Operation(
            summary = "Invalidate country cache",
            description = "Clears the cached country information to force refresh from the Lexoffice API. Requires admin scope.",
            security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cache invalidated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid token",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - insufficient privileges (requires admin scope)",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public ResponseEntity<String> invalidateCountryCache() {
        countryService.invalidateCache();
        return ResponseEntity.ok("Country cache invalidated successfully");
    }
}