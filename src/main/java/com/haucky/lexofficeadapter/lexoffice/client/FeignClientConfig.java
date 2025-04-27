package com.haucky.lexofficeadapter.lexoffice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration for Feign clients.
 */
@Configuration
public class FeignClientConfig {

    @Value("${lexoffice.api.token}")
    private String apiToken;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FeignClientConfig.class);

    @Bean
    public Client feignClient() {
        return new LoggingClient(new feign.okhttp.OkHttpClient());
    }

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new LexofficeErrorDecoder(objectMapper);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor authorizationInterceptor() {
        return template -> {
            String token = "Bearer " + apiToken;
            template.header("Authorization", token);
            // Log a masked version of the token
            String maskedToken = apiToken.substring(0, Math.min(5, apiToken.length())) + "...";
            logger.info("Adding Authorization header: Bearer {}", maskedToken);
        };
    }

    static class LoggingClient implements Client {
        private final Client delegate;

        public LoggingClient(Client delegate) {
            this.delegate = delegate;
        }

        @Override
        public Response execute(Request request, Request.Options options) throws IOException {
            // Log basic info at INFO level
            logger.info("Executing request: {} {}", request.httpMethod().name(), request.url());

            if (logger.isDebugEnabled()) {
                logDetailedRequest(request);
            }

            try {
                Response response = delegate.execute(request, options);

                if (logger.isDebugEnabled()) {
                    logDetailedResponse(response);
                }

                return response;
            } catch (Exception e) {
                logger.error("Error executing request: {}", e.getMessage(), e);
                throw e;
            }
        }

        private void logDetailedRequest(Request request) {
            logger.debug("=== REQUEST ===");
            logger.debug("URL: {} {}", request.httpMethod().name(), request.url());
            logger.debug("Headers: ");

            request.headers().forEach((name, values) -> {
                values.forEach(value -> {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        if (value.length() > 15) {
                            logger.debug("  {}: Bearer {}...", name, value.substring(7, 15));
                        } else {
                            logger.debug("  {}: [MISSING OR MALFORMED]", name);
                        }
                    } else {
                        logger.debug("  {}: {}", name, value);
                    }
                });
            });
        }

        private void logDetailedResponse(Response response) {
            logger.debug("=== RESPONSE ===");
            logger.debug("Status: {}", response.status());
            logger.debug("Headers: ");

            response.headers().forEach((name, values) -> {
                values.forEach(value -> logger.debug("  {}: {}", name, value));
            });
        }
    }
}