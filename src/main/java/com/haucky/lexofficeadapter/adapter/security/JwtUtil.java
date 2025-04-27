package com.haucky.lexofficeadapter.adapter.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility class for generating test JWT tokens
 */
@Getter
@Controller
public class JwtUtil {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public static final String ISSUER = "Dummy Authorization Server";

    public String generateAdminToken() {
        Instant now = Instant.now();
        
        return Jwts.builder()
                .setSubject("admin-service")
                .setIssuer(ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .claim("scope", "data:read data:write admin")
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
    
    public String generateUserToken() {
        Instant now = Instant.now();
        
        return Jwts.builder()
                .setSubject("user")
                .setIssuer("test-token-generator")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .claim("scope", "data:read data:write")
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}