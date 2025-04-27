package com.haucky.lexofficeadapter.unit;

import com.haucky.lexofficeadapter.lexoffice.client.FeignClientConfig;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignClientConfigTest {

    private FeignClientConfig feignClientConfig;

    @BeforeEach
    void setUp() {
        feignClientConfig = new FeignClientConfig();
    }

    @Test
    void givenApiToken_whenAuthorizationInterceptorApplied_thenAuthorizationHeaderIsAdded() {
        // Arrange
        String apiToken = "test-token";
        ReflectionTestUtils.setField(feignClientConfig, "apiToken", apiToken);
        RequestTemplate template = new RequestTemplate();

        // Act
        feignClientConfig.authorizationInterceptor().apply(template);

        // Assert
        Map<String, Collection<String>> headers = template.headers();
        assertThat(headers).containsKey("Authorization");
        assertThat(headers.get("Authorization")).hasSize(1);
        assertThat(headers.get("Authorization").iterator().next()).contains("Bearer " + apiToken);
    }
}