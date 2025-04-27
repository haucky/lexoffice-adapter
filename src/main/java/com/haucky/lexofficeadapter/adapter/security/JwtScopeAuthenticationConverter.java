package com.haucky.lexofficeadapter.adapter.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class JwtScopeAuthenticationConverter extends JwtAuthenticationConverter {

    public JwtScopeAuthenticationConverter() {
        super();
        setJwtGrantedAuthoritiesConverter(jwt -> {
            String scope = jwt.getClaimAsString("scope");
            if (scope == null || scope.isEmpty()) {
                return Collections.emptyList();
            }

            return Arrays.stream(scope.split("\\s+"))
                    .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                    .collect(Collectors.toList());
        });
    }
}