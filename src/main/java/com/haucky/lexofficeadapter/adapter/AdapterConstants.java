package com.haucky.lexofficeadapter.adapter;

import java.net.URI;

/**
 * Constants class for Lexoffice API related values
 */
public final class AdapterConstants {

    // Private constructor to prevent instantiation
    private AdapterConstants() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }
    
    // API Documentation URIs
    public static final URI ADAPTER_ERROR_DOCS = URI.create("https://lexoffice-adapter.bulbt.com/api.html");
}