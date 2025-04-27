package com.haucky.lexofficeadapter.unit;

import static com.haucky.lexofficeadapter.adapter.AdapterConstants.ADAPTER_ERROR_DOCS;
import static com.haucky.lexofficeadapter.utils.TestUtils.loadJsonFromFile;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.haucky.lexofficeadapter.adapter.controller.ContactController;
import com.haucky.lexofficeadapter.common.dto.mapper.ContactMapperImpl;
import com.haucky.lexofficeadapter.lexoffice.LexofficeCountryService;
import com.haucky.lexofficeadapter.lexoffice.dto.Contact;
import com.haucky.lexofficeadapter.lexoffice.dto.ContactsPage;
import com.haucky.lexofficeadapter.lexoffice.LexofficeContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false,print = MockMvcPrint.NONE)
public class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LexofficeContactService lexofficeContactService;
    @MockitoBean
    private ContactMapperImpl contactMapper;
    @MockitoBean
    private LexofficeCountryService lexofficeCountryService;

    @BeforeEach
    public void setupBeforeEach() {
        // I'm not a big fan of mocking with behavior, apart from LexofficeCountryService (which is needed because it is part of the validation layer) no service is mocked here.
        // Instead, I prefer to test the happy paths in an integration test, as this is needed anyway.
        // Controller tests here mostly test for request validations or logic which is not part of the service layer.
        when(lexofficeCountryService.isValidCountryCode(ArgumentMatchers.anyString())).thenReturn(true);
    }

    @Nested
    class CreateContactTests {
        @Test
        public void givenValidContactData_whenPostingToContactsEndpoint_thenReturnSuccessWithContactDetails() throws Exception {

            // Arrange
            String requestBody = loadJsonFromFile("test-data/valid-contact-request-complete.json");

            // Act & Assert
            mockMvc.perform(post("/v1/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
        }

        @Test
        public void givenInvalidContactData_whenPostingToContactsEndpoint_thenReturnValidationErrors() throws Exception {
            // Arrange
            String requestBody = loadJsonFromFile("test-data/invalid-contact-request-invalid-fields.json");

            // Act & Assert
            mockMvc.perform(post("/v1/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print()) // Print the response for debugging
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.type", is(ADAPTER_ERROR_DOCS.toString())))
                    .andExpect(jsonPath("$.title", is("Validation Error")))
                    .andExpect(jsonPath("$.status", is(422)))
                    .andExpect(jsonPath("$.detail", is("The request contains invalid parameters")))
                    .andExpect(jsonPath("$.instance").exists())

                    .andExpect(jsonPath("$.validationErrors", hasSize(9)))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'company.name')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'company.contactPersons[0].emailAddress')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'company.contactPersons[0].salutation')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'addresses.billing[0].countryCode')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'addresses.shipping[0].countryCode')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'emailAddresses.business[0]')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'emailAddresses.office[0]')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'emailAddresses.other[0]')]").exists())
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'note')]").exists())
                    .andReturn();
        }

        @Test
        public void givenContactDataWithUnknownFields_whenPostingToContactsEndpoint_thenReturnBadRequest() throws Exception {
            // Arrange
            String requestBody = loadJsonFromFile("test-data/invalid-contact-request-unknown-fields.json");

            // Act & Assert
            mockMvc.perform(post("/v1/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print()) // Print the response for debugging
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type", is(ADAPTER_ERROR_DOCS.toString())))
                    .andExpect(jsonPath("$.title", is("Validation Error")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Unknown field in request: 'extraField' in Company")))
                    .andExpect(jsonPath("$.instance").exists());
        }
    }

    @Nested
    class GetContactByIdTests {
        @Test
        public void givenValidId_whenGettingContactByIdEndpoint_thenReturnContact() throws Exception {
            // Arrange
            UUID validId = UUID.randomUUID();
            Contact mockContact = new Contact();
            mockContact.setId(validId);
            mockContact.setVersion(1);

            // Act & Assert
            mockMvc.perform(get("/v1/contacts/{id}", validId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        public void givenInvalidId_whenGettingContactByIdEndpoint_thenReturnBadRequest() throws Exception {
            // Arrange
            String invalidId = "not-a-valid-uuid";

            // Act & Assert
            mockMvc.perform(get("/v1/contacts/{id}", invalidId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type", is(ADAPTER_ERROR_DOCS.toString())))
                    .andExpect(jsonPath("$.title", is("Invalid Parameter")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", containsString("not-a-valid-uuid")))
                    .andExpect(jsonPath("$.instance").exists());
        }
    }

    @Nested
    class ListContactsTests {
        @Test
        public void givenValidPagingParameters_whenGettingAllContactsEndpoint_thenReturnSuccess() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/v1/contacts")
                            .param("page", "0")
                            .param("size", "25")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        public void givenTooLargeSizeParameter_whenGettingAllContactsEndpoint_thenReturnBadRequest() throws Exception {
            // Arrange
            int page = 0;
            int size = 300; // Over the 250 limit

            // Act & Assert
            mockMvc.perform(get("/v1/contacts")
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.type", is(ADAPTER_ERROR_DOCS.toString())))
                    .andExpect(jsonPath("$.title", is("Validation Error")))
                    .andExpect(jsonPath("$.status", is(422)))
                    .andExpect(jsonPath("$.detail", is("The request contains invalid parameters")))
                    .andExpect(jsonPath("$.instance").exists())
                    .andExpect(jsonPath("$.validationErrors", hasSize(1)))
                    .andExpect(jsonPath("$.validationErrors[0].field", containsString("size")))
                    .andExpect(jsonPath("$.validationErrors[0].message", containsString("must be less than or equal to 250")));
        }

        @Test
        public void givenValidFilterParameters_whenGettingContacts_thenReturnSuccess() throws Exception {
            // Arrange
            ContactsPage mockContactsPage = new ContactsPage();
            mockContactsPage.setContent(Collections.emptyList());
            mockContactsPage.setTotalElements(0);

            // Act & Assert
            mockMvc.perform(get("/v1/contacts")
                            .param("page", "0")
                            .param("size", "25")
                            .param("email", "test@example.com")
                            .param("name", "Test Company")
                            .param("number", "12345")
                            .param("customer", "true")
                            .param("vendor", "false")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        public void givenInvalidFilterParameters_whenGettingContacts_thenReturnValidationErrors() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/v1/contacts")
                            .param("page", "0")
                            .param("size", "25")
                            .param("email", "a")             // Too short (less than 3 chars)
                            .param("name", "B")              // Too short (less than 3 chars)
                            .param("number", "-10")          // Negative number (not valid for a contact number)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.type", is(ADAPTER_ERROR_DOCS.toString())))
                    .andExpect(jsonPath("$.title", is("Validation Error")))
                    .andExpect(jsonPath("$.status", is(422)))
                    .andExpect(jsonPath("$.detail", is("The request contains invalid parameters")))
                    .andExpect(jsonPath("$.instance").exists())
                    .andExpect(jsonPath("$.validationErrors", hasSize(3)))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'email')].message", hasItem(containsString("at least 3 characters"))))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')].message", hasItem(containsString("at least 3 characters"))))
                    .andExpect(jsonPath("$.validationErrors[?(@.field == 'number')].message", hasItem(containsString("must be positive"))));
        }
    }
}