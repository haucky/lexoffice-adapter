package com.haucky.lexofficeadapter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LexofficeAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LexofficeAdapterApplication.class, args);
    }

}
