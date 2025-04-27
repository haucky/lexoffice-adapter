package com.haucky.lexofficeadapter.unit;

import com.haucky.lexofficeadapter.adapter.exception.AdapterException;
import com.haucky.lexofficeadapter.common.dto.Country;
import com.haucky.lexofficeadapter.lexoffice.LexofficeCountryService;
import com.haucky.lexofficeadapter.lexoffice.client.LexofficeFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static com.haucky.lexofficeadapter.lexoffice.LexofficeConstants.LEXOFFICE_REQUEST_ID_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LexofficeCountryServiceTest {

    @Mock
    private LexofficeFeignClient lexofficeClient;

    @InjectMocks
    private LexofficeCountryService countryService;

    private static final String REQUEST_ID = "test-request-id-123";
    private static final String EXISTING_COUNTRY_CODE = "DE";
    private static final String NON_EXISTING_COUNTRY_CODE = "XX";
    private static final String COUNTRY_NAME = "Germany";

    @BeforeEach
    void setUp() {
        Country country = Country.builder()
                .countryCode(EXISTING_COUNTRY_CODE)
                .countryNameEN(COUNTRY_NAME)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(LEXOFFICE_REQUEST_ID_KEY, REQUEST_ID);

        ResponseEntity<List<Country>> response = new ResponseEntity<>(
                Collections.singletonList(country),
                headers,
                HttpStatus.OK
        );

        when(lexofficeClient.getCountries()).thenReturn(response);
        countryService.refreshCountryCache();
    }

    @Test
    void givenExistingCountryCode_whenConvertingToName_thenReturnCorrectName() {
        // Act
        String countryName = countryService.convertCountryCodeToName(EXISTING_COUNTRY_CODE);

        // Assert
        assertEquals(COUNTRY_NAME, countryName);
        verify(lexofficeClient, times(1)).getCountries(); // Verify client was called once during setup
    }

    @Test
    void givenNonExistingCountryCode_whenConvertingToName_thenThrowAdapterExceptionWithRequestId() {
        // Act & Assert
        AdapterException exception = assertThrows(AdapterException.class, () -> 
            countryService.convertCountryCodeToName(NON_EXISTING_COUNTRY_CODE)
        );

        assertEquals("Country mapping not found", exception.getTitle());
        assertEquals("Country name was not found for country code: " + NON_EXISTING_COUNTRY_CODE, exception.getDetail());
        assertEquals(REQUEST_ID, exception.getUpstreamRequestId());
        verify(lexofficeClient, times(1)).getCountries(); // Verify client was called once during setup
    }
}