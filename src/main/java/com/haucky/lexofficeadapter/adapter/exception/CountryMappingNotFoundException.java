package com.haucky.lexofficeadapter.adapter.exception;

/**
 * Exception thrown when a country mapping cannot be found for a given country code.
 * <p>
 * Since country codes are checked on creation, this exception can only occur in very rare cases
 * and is typically the fault of an upstream system (either data containing unsupported country codes
 * or invalid country mappings).
 * <p>
 * Upstream request ID should be extracted from the last cached countries request.
 *
 */
public class CountryMappingNotFoundException extends AdapterException {
    public CountryMappingNotFoundException(String countryCode, String upstreamRequestId) {
        super("Country mapping not found",
                "Country name was not found for country code: " + countryCode, upstreamRequestId);
    }
}