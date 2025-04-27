package com.haucky.lexofficeadapter.lexoffice;

import java.net.URI;

/**
 * Constants class for Lexoffice API related values
 */
public final class LexofficeConstants {
    
    private LexofficeConstants() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    // TODO Ideally those would point to specific error descriptions
    public static final URI LEXOFFICE_TOKEN_DOCS = URI.create("https://developers.lexoffice.io/docs/#lexoffice-api-documentation-introduction");
    public static final URI LEXOFFICE_CONTACT_DOCS = URI.create("https://developers.lexoffice.io/docs/#contacts-endpoint-purpose");
    public static URI LEXOFFICE_STATUS_DOCS = URI.create("https://developers.lexoffice.io/docs/#faq-stay-informed-about-the-system-status");
    public static String LEXOFFICE_REQUEST_ID_KEY = "x-amzn-requestid";
}