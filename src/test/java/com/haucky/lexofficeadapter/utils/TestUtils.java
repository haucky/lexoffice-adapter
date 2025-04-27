package com.haucky.lexofficeadapter.utils;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.util.StreamUtils.copyToString;

public class TestUtils {

    public static String loadJsonFromFile(String filePath) throws IOException {
        return copyToString(new ClassPathResource(filePath).getInputStream(), StandardCharsets.UTF_8);
    }
}
