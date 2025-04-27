package com.haucky.lexofficeadapter.adapter.controller;

import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.adapter.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller with endpoints to test the security configuration
 */
@RestController
@RequestMapping("/v1/tokens")
@Tag(name = "Authorization", description = "Operations for JWT token management and authentication testing")
public class AuthorizationController {
    private final JwtUtil jwtUtil;

    public AuthorizationController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/admin")
    @Operation(
            summary = "Generate admin token",
            description = "Generates a JWT token with admin privileges for testing"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token generated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public String getAdminToken() {
        return jwtUtil.generateAdminToken();
    }

    @GetMapping("/user")
    @Operation(
            summary = "Generate user token",
            description = "Generates a JWT token with standard user privileges for testing"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token generated successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public String getUserToken() {
        return jwtUtil.generateUserToken();
    }

    @GetMapping("/authenticated")
    @Operation(
            summary = "Test authentication",
            description = "A test endpoint that requires a valid JWT token to access, confirming authentication is working",
            security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid token",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = Problem.class))
            )
    })
    public String authenticatedEndpoint() {
        return "You are authenticated with a valid token!";
    }
}