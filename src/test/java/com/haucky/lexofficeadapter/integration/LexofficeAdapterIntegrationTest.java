package com.haucky.lexofficeadapter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.haucky.lexofficeadapter.adapter.dto.AddressResponse;
import com.haucky.lexofficeadapter.adapter.dto.ContactResponse;
import com.haucky.lexofficeadapter.adapter.dto.ContactsPageResponse;
import com.haucky.lexofficeadapter.adapter.dto.problem.Problem;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationError;
import com.haucky.lexofficeadapter.adapter.dto.problem.ValidationProblem;
import com.haucky.lexofficeadapter.lexoffice.dto.Address;
import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.common.dto.requests.ContactFilterRequest;
import com.haucky.lexofficeadapter.common.dto.requests.ContactPageRequest;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactCreated;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.haucky.lexofficeadapter.lexoffice.LexofficeConstants.LEXOFFICE_REQUEST_ID_KEY;
import static com.haucky.lexofficeadapter.utils.TestUtils.loadJsonFromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 9090)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class LexofficeAdapterIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String adapterEndpoint = "/v1/contacts";
    private final String lexContactEndpoint = "/contacts";
    private final String lexCountriesEndpoint = "/countries";

    @BeforeEach
    public void setup() throws IOException {
        WireMock.reset();
        setupCountriesStub();
    }

    @AfterEach
    public void teardown() {
        invalidateCountriesCache();
    }

    // POST /v1/contacts tests
    @Nested
    class CreateContactTests {
        @Test
        public void givenValidContact_whenPostToContactsEndpointAndUpstreamApiSucceeds_thenReturnSuccess() throws Exception {
            // Arrange
            String contactRequest = loadJsonFromFile("test-data/valid-contact-request-complete.json");

            String mockResponseBody = loadJsonFromFile("test-data/valid-contact-creation-response.json");
            stubFor(post(urlEqualTo(lexContactEndpoint))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockResponseBody)));

            // Act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(contactRequest, headers);

            ResponseEntity<ContactCreated> response = assertDoesNotThrow(() ->
                            restTemplate.exchange(
                                    "http://localhost:" + port + adapterEndpoint,
                                    HttpMethod.POST,
                                    requestEntity,
                                    ContactCreated.class),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            ContactCreated expectedResponse = objectMapper.readValue(mockResponseBody, ContactCreated.class);
            ContactCreated actualResponse = response.getBody();
            assertThat(actualResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponse);
        }

        @Test
        public void givenValidContact_whenPostToContactsEndpointAndUpstreamApiReturnsError_thenPropagateErrorCorrectly() throws Exception {
            // Arrange
            String contactRequest = loadJsonFromFile("test-data/valid-contact-request-complete.json");
            String errorResponse = "{ \"message\" : \"Unauthorized\" }";

            String requestIdLex = "dcf69c72-e2c4-4580-9895-f03dbb4ca0";

            stubFor(post(urlEqualTo(lexContactEndpoint))
                    .willReturn(aResponse()
                            .withStatus(401)
                            .withBody(errorResponse)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withHeader(LEXOFFICE_REQUEST_ID_KEY, requestIdLex)));
            // Act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(contactRequest, headers);

            ResponseEntity<Problem> response = assertDoesNotThrow(() ->
                            restTemplate.exchange(
                                    "http://localhost:" + port + adapterEndpoint,
                                    HttpMethod.POST,
                                    requestEntity,
                                    Problem.class),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            Problem problem = response.getBody();
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(problem.getTitle()).isEqualTo("Lexoffice API Authorization Error");
            assertThat(problem.getDetail()).isEqualTo("Note: Currently, the client is responsible for refreshing the token.\n" +
                    "Reason: "+errorResponse);
            assertThat(problem.getInstance()).isEqualTo(URI.create(adapterEndpoint +"/upstream-request/"+requestIdLex));
        }

        @Test
        public void givenContactWithNotAvailableCountryCode_whenPostToContactsEndpoint_thenReturnValidationError() throws Exception {
            // Arrange
            String contactRequest = loadJsonFromFile("test-data/invalid-contact-request-invalid-fields.json");

            String mockResponseBody = loadJsonFromFile("test-data/valid-contact-creation-response.json");
            stubFor(post(urlEqualTo(lexContactEndpoint))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockResponseBody)));

            // Act
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(contactRequest, headers);

            ResponseEntity<ValidationProblem> response = assertDoesNotThrow(() ->
                            restTemplate.exchange(
                                    "http://localhost:" + port + adapterEndpoint,
                                    HttpMethod.POST,
                                    requestEntity,
                                    ValidationProblem.class),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            ValidationProblem problem = response.getBody();
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(problem.getTitle()).isEqualTo("Validation Error");
            Stream<ValidationError> validationErrorStream = problem.getValidationErrors().stream().filter(e -> e.getField().equals("addresses.billing[0].countryCode"));
            assertTrue(validationErrorStream.anyMatch(e -> e.getMessage().equals("country code not available")));
        }
    }

    // GET /v1/contacts/{id} tests
    @Nested
    class GetContactByIdTests {
        @Test
        public void givenValidId_whenGetContactByIdEndpointAndUpstreamApiSucceeds_thenReturnContactAndIgnoreUnknownFields() throws Exception {
            // Arrange
            UUID validId = UUID.randomUUID();
            String mockResponseBody = loadJsonFromFile("test-data/valid-contact-response.json");

            stubFor(get(urlEqualTo(lexContactEndpoint + "/" + validId))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockResponseBody)));

            // Act
            ResponseEntity<ContactResponse> response = assertDoesNotThrow(() ->
                            restTemplate.getForEntity(
                                    "http://localhost:" + port + adapterEndpoint + "/{id}",
                                    ContactResponse.class,
                                    validId),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Contact expectedContact = objectMapper.readValue(mockResponseBody, Contact.class);
            ContactResponse actualContactResponse = response.getBody();
            assertThat(actualContactResponse)
                    .usingRecursiveComparison()
                    .ignoringFieldsOfTypes(AddressResponse.class, Address.class)
                    .isEqualTo(expectedContact);
            assertThat(actualContactResponse.getAddresses().getBilling().get(0).getCountryName()).isEqualTo("Germany");
            assertThat(expectedContact.getAddresses().getBilling().get(0).getCountryCode()).isEqualTo("DE");
        }

        @Test
        public void givenNonExistingCountryCode_whenGetContactByIdEndpointAndContactRetrievalSucceeds_thenReturnNotFound() throws Exception {
            // Arrange
            UUID validContactId = UUID.randomUUID();
            String mockResponseBody = loadJsonFromFile("test-data/invalid-contact-response.json");

            stubFor(get(urlEqualTo(lexContactEndpoint + "/" + validContactId))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockResponseBody)));

            // Act
            ResponseEntity<Problem> response = assertDoesNotThrow(() ->
                            restTemplate.getForEntity(
                                    "http://localhost:" + port + adapterEndpoint + "/{id}",
                                    Problem.class,
                                    validContactId),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            Problem problem = response.getBody();
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(problem.getTitle()).isEqualTo("Country mapping not found");
            assertThat(problem.getDetail()).isEqualTo("Country name was not found for country code: XX");
        }

        @Test
        public void givenValidId_whenGetContactByIdEndpointAndUpstreamApiReturnsNotFound_thenPropagateError() throws Exception {
            // Arrange
            UUID validId = UUID.randomUUID();
            String errorResponse = "{ \"message\": \"Contact not found\" }";
            String requestIdLex = "dcf69c72-e2c4-4580-9895-f03dbb4ca1";

            stubFor(get(urlEqualTo(lexContactEndpoint + "/" + validId))
                    .willReturn(aResponse()
                            .withStatus(404)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withHeader(LEXOFFICE_REQUEST_ID_KEY, requestIdLex)
                            .withBody(errorResponse)));

            // Act
            ResponseEntity<Problem> response = assertDoesNotThrow(() ->
                            restTemplate.exchange(
                                    "http://localhost:" + port + adapterEndpoint + "/{id}",
                                    HttpMethod.GET,
                                    null,
                                    Problem.class,
                                    validId),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            Problem problem = response.getBody();
            assertThat(problem).isNotNull();
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(problem.getTitle()).isEqualTo("Lexoffice API Error: Not Found");
            assertThat(problem.getDetail()).contains("Not Found");
            assertThat(problem.getInstance()).isEqualTo(URI.create(adapterEndpoint + "/" + validId + "/upstream-request/" + requestIdLex));
        }

        @Test
        public void givenInvalidId_whenGetContactByIdEndpoint_thenReturnMethodArgumentTypeMismatchError() {
            // Arrange
            String invalidId = "not-a-uuid";

            // Act
            ResponseEntity<ValidationProblem> response = assertDoesNotThrow(() ->
                            restTemplate.getForEntity(
                                    "http://localhost:" + port + adapterEndpoint + "/{id}",
                                    ValidationProblem.class,
                                    invalidId),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ValidationProblem problem = response.getBody();
            assertThat(problem).isNotNull();
            assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(problem.getTitle()).isEqualTo("Invalid Parameter");
            assertThat(problem.getDetail()).contains("Failed to convert value '" + invalidId + "' to required type 'UUID'");

            assertThat(problem.getValidationErrors()).hasSize(1);
            ValidationError error = problem.getValidationErrors().get(0);
            assertThat(error.getField()).isEqualTo("id");
            assertThat(error.getMessage()).isEqualTo("Invalid format");
        }
    }

    @Nested
    class CachingTests {
        @Test
        public void givenNonExistingCountryCode_whenGetContactByIdEndpointAndContactRetrievalSucceeds_thenReturnNotFoundWithCachedUpstreamRequestId() throws Exception {
            // Arrange
            UUID validContactId = UUID.randomUUID();
            String mockResponseBody = loadJsonFromFile("test-data/invalid-contact-response.json");
            stubFor(get(urlEqualTo(lexContactEndpoint + "/" + validContactId))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockResponseBody)));

            String initialRequestId = setupCountriesStub();

            // Act
            // 1. Invalidate cache
            invalidateCountriesCache();

            // 2. Call Endpoint 1.
            ResponseEntity<Problem> response1 = assertDoesNotThrow(() ->
                            restTemplate.getForEntity(
                                    "http://localhost:" + port + adapterEndpoint + "/{id}",
                                    Problem.class,
                                    validContactId),
                    "Exception thrown during REST call or deserialization"
            );

            // 3. Call Endpoint 2.
            ResponseEntity<Problem> response2 = assertDoesNotThrow(() ->
                            restTemplate.getForEntity(
                                    "http://localhost:" + port + adapterEndpoint + "/{id}",
                                    Problem.class,
                                    validContactId),
                    "Exception thrown during REST call or deserialization"
            );


            // Assert that request id is also cached
            Problem problem1 = response1.getBody();
            assertThat(problem1.getInstance()).isEqualTo(URI.create("/v1/contacts/"+validContactId+ "/upstream-request/"+initialRequestId));

            Problem problem2 = response2.getBody();
            assertThat(problem2.getInstance()).isEqualTo(URI.create("/v1/contacts/"+validContactId+ "/upstream-request/"+initialRequestId));

            verify(1, getRequestedFor(urlPathEqualTo(lexCountriesEndpoint)));
        }
    }

    // GET /v1/contacts/ tests
    @Nested
    class ListContactsTests {
        @Test
        public void givenAllFilterParametersAndPagination_whenGetContacts_thenReturnFilteredContactsPage() throws Exception {
            // Arrange
            ContactPageRequest pageRequest = new ContactPageRequest();
            pageRequest.setPage(2);
            pageRequest.setSize(50);

            ContactFilterRequest filterRequest = new ContactFilterRequest();
            filterRequest.setEmail("test@example.com");
            filterRequest.setName("Test Company");
            filterRequest.setNumber(12345);
            filterRequest.setCustomer(true);
            filterRequest.setVendor(false);

            String mockResponseBody = loadJsonFromFile("test-data/valid-contacts-response.json");

            stubFor(get(urlPathEqualTo(lexContactEndpoint))
                    .withQueryParam("page", equalTo(String.valueOf(pageRequest.getPage())))
                    .withQueryParam("size", equalTo(String.valueOf(pageRequest.getSize())))
                    .withQueryParam("email", equalTo(filterRequest.getEmail()))
                    .withQueryParam("name", equalTo(filterRequest.getName()))
                    .withQueryParam("number", equalTo(String.valueOf(filterRequest.getNumber())))
                    .withQueryParam("customer", equalTo(filterRequest.getCustomer().toString()))
                    .withQueryParam("vendor", equalTo(filterRequest.getVendor().toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockResponseBody)));

            setupCountriesStub();

            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl("http://localhost:" + port + adapterEndpoint)
                    .queryParam("page", pageRequest.getPage())
                    .queryParam("size", pageRequest.getSize())
                    .queryParam("email", filterRequest.getEmail())
                    .queryParam("name", filterRequest.getName())
                    .queryParam("number", filterRequest.getNumber())
                    .queryParam("customer", filterRequest.getCustomer())
                    .queryParam("vendor", filterRequest.getVendor());

            // Act
            ResponseEntity<ContactsPageResponse> response = assertDoesNotThrow(() ->
                            restTemplate.exchange(
                                    uriBuilder.toUriString(),
                                    HttpMethod.GET,
                                    null,
                                    ContactsPageResponse.class),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            ContactsPageResponse contactsPage = response.getBody();
            assertThat(contactsPage).isNotNull();
            assertThat(contactsPage.getContent()).isNotEmpty();

            assertThat(contactsPage.getContent().get(0).getAddresses().getBilling().get(0).getCountryName()).isEqualTo("Germany");

            verify(getRequestedFor(urlPathEqualTo(lexContactEndpoint))
                    .withQueryParam("page", equalTo(String.valueOf(pageRequest.getPage())))
                    .withQueryParam("size", equalTo(String.valueOf(pageRequest.getSize())))
                    .withQueryParam("email", equalTo(filterRequest.getEmail()))
                    .withQueryParam("name", equalTo(filterRequest.getName()))
                    .withQueryParam("number", equalTo(String.valueOf(filterRequest.getNumber())))
                    .withQueryParam("customer", equalTo(filterRequest.getCustomer().toString()))
                    .withQueryParam("vendor", equalTo(filterRequest.getVendor().toString())));
        }

        @Test
        public void givenValidPaginationAndSomeFilterParameters_whenGetContactsAndUpstreamApiSucceeds_thenSendOnlyNonEmptyParameters() throws Exception {
            // Arrange
            ContactPageRequest pageRequest = new ContactPageRequest();
            pageRequest.setPage(0);
            pageRequest.setSize(25);

            ContactFilterRequest filterRequest = new ContactFilterRequest();
            filterRequest.setEmail("info@example.com");
            filterRequest.setName("Corporation");
            filterRequest.setCustomer(true);

            String mockContactResponseBody = loadJsonFromFile("test-data/valid-contacts-response.json");

            stubFor(get(urlPathEqualTo(lexContactEndpoint))
                    .withQueryParam("page", equalTo(String.valueOf(pageRequest.getPage())))
                    .withQueryParam("size", equalTo(String.valueOf(pageRequest.getSize())))
                    .withQueryParam("email", equalTo(filterRequest.getEmail()))
                    .withQueryParam("name", equalTo(filterRequest.getName()))
                    .withQueryParam("customer", equalTo(filterRequest.getCustomer().toString()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withBody(mockContactResponseBody)));

            setupCountriesStub();

            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl("http://localhost:" + port + adapterEndpoint)
                    .queryParam("page", pageRequest.getPage())
                    .queryParam("size", pageRequest.getSize())
                    .queryParam("email", filterRequest.getEmail())
                    .queryParam("name", filterRequest.getName())
                    .queryParam("customer", filterRequest.getCustomer());

            // Act
            ResponseEntity<ContactsPageResponse> response = assertDoesNotThrow(() ->
                            restTemplate.exchange(
                                    uriBuilder.toUriString(),
                                    HttpMethod.GET,
                                    null,
                                    ContactsPageResponse.class),
                    "Exception thrown during REST call or deserialization"
            );

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(getRequestedFor(urlPathEqualTo(lexContactEndpoint))
                    .withQueryParam("page", equalTo(String.valueOf(pageRequest.getPage())))
                    .withQueryParam("size", equalTo(String.valueOf(pageRequest.getSize())))
                    .withQueryParam("email", equalTo(filterRequest.getEmail()))
                    .withQueryParam("name", equalTo(filterRequest.getName()))
                    .withQueryParam("customer", equalTo(filterRequest.getCustomer().toString())));
            verify(0, getRequestedFor(urlPathEqualTo(lexContactEndpoint))
                    .withQueryParam("page", equalTo(String.valueOf(pageRequest.getPage())))
                    .withQueryParam("size", equalTo(String.valueOf(pageRequest.getSize())))
                    .withQueryParam("email", equalTo(filterRequest.getEmail()))
                    .withQueryParam("name", equalTo(filterRequest.getName()))
                    .withQueryParam("number", equalTo(filterRequest.getName()))
                    .withQueryParam("vendor", equalTo(filterRequest.getName()))
                    .withQueryParam("customer", equalTo(filterRequest.getCustomer().toString())));
        }
    }


    private String setupCountriesStub() throws IOException {
        String mockCountryResponseBody = loadJsonFromFile("test-data/valid-countries-response.json");
        String requestId = UUID.randomUUID().toString();
        stubFor(get(urlPathEqualTo(lexCountriesEndpoint))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(LEXOFFICE_REQUEST_ID_KEY,requestId)
                        .withBody(mockCountryResponseBody)));
        return requestId;
    }

    private void invalidateCountriesCache() {
        restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/cache/countries/invalidate",
                null,
                String.class);
    }
}