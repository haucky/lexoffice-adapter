package com.haucky.lexofficeadapter.lexoffice;

import com.haucky.lexofficeadapter.adapter.exception.CountryMappingNotFoundException;
import com.haucky.lexofficeadapter.common.dto.Country;
import com.haucky.lexofficeadapter.lexoffice.client.LexofficeFeignClient;
import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.haucky.lexofficeadapter.lexoffice.LexofficeConstants.LEXOFFICE_REQUEST_ID_KEY;

/**
 * Service for interacting with countries, wrapping the lexoffice client
 */
@Service
public class LexofficeCountryService {
    private static final Logger log = LoggerFactory.getLogger(LexofficeCountryService.class);
    private final LexofficeFeignClient lexofficeClient;

    private Map<String, Country> countryCache;
    private final AtomicReference<String> lastRequestId = new AtomicReference<>();


    public LexofficeCountryService(LexofficeFeignClient lexofficeClient) {
        this.lexofficeClient = lexofficeClient;
    }

    @Named("countryCodeToName")
    public String convertCountryCodeToName(String countryCode) {
        if (countryCode == null) {
            return null;
        }

        if(countryCache == null) {
            refreshCountryCache();
        }

        Country country = countryCache.get(countryCode);

        if (country != null) {
            return country.getCountryNameEN();
        } else {
            throw new CountryMappingNotFoundException(countryCode, lastRequestId.get());
        }
    }

    public boolean isValidCountryCode(String countryCode) {
        if(countryCache == null) {
            refreshCountryCache();
        }

        if(countryCode == null || countryCode.isEmpty()) {
            return false;
        }

        return countryCache.containsKey(countryCode);
    }

    @Scheduled(fixedRate = 7200000) // 2 hours in milliseconds
    public void refreshCountryCache() {
            log.info("Refreshing country cache");
            ResponseEntity<List<Country>> countriesResponse = lexofficeClient.getCountries();

            Map<String, Country> newCache = new ConcurrentHashMap<>();

            for (Country country : Objects.requireNonNull(countriesResponse.getBody())) {
                newCache.put(country.getCountryCode(), country);
            }

            this.countryCache = newCache;
            this.lastRequestId.set(countriesResponse.getHeaders().getFirst(LEXOFFICE_REQUEST_ID_KEY));

            log.info("Country cache refreshed with {} entries", newCache.size());
    }

    public void invalidateCache() {
        log.info("Invalidating country cache");
        this.countryCache = null;
        this.lastRequestId.set(null);
        log.info("Country cache invalidated");
    }
}