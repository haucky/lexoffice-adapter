package com.haucky.lexofficeadapter.adapter.config;

import com.haucky.lexofficeadapter.adapter.security.JwtAuthenticationFilter;
import com.haucky.lexofficeadapter.adapter.security.JwtUtil;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtSecretKey());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v1/tokens/admin", "v1/tokens/user").permitAll()
                        .requestMatchers("/v1/cache/**").hasAuthority("SCOPE_admin")
                        .requestMatchers("/", "/v1/api-docs/**", "/api.html", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/contacts/**").hasAuthority("SCOPE_data:read")
                        .requestMatchers(HttpMethod.POST, "/v1/contacts/**").hasAuthority("SCOPE_data:write")
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_admin")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(jwtUtil().getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}