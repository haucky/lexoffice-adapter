package com.haucky.lexofficeadapter.adapter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final SecretKey secretKey;

    public JwtAuthenticationFilter(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String scope = claims.get("scope", String.class);
            
            Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
            
            if (scope != null) {
                authorities = Arrays.stream(scope.split("\\s+"))
                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                        .collect(Collectors.toList());
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    subject, null, authorities);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            // In case of invalid token, clear the security context
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}