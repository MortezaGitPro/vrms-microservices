package com.champsoft.vrms.registration.api;

import com.champsoft.vrms.registration.application.port.out.AgentEligibilityPort;
import com.champsoft.vrms.registration.application.port.out.OwnerEligibilityPort;
import com.champsoft.vrms.registration.application.port.out.VehicleEligibilityPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test → tests the Registration API layer with Spring Boot
// Uses MockMvc to send fake HTTP requests to the controller
// Uses mocked eligibility ports instead of calling real cars/owners/agents services
// Uses the "testing" profile, usually with an H2 database
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
class RegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Mocked outbound port:
    // Registration service depends on cars-service to check vehicle eligibility.
    // In this test, we fake that dependency using Mockito.
    @MockitoBean
    private VehicleEligibilityPort vehicleEligibilityPort;

    // Mocked outbound port:
    // Registration service depends on owners-service to check owner eligibility.
    @MockitoBean
    private OwnerEligibilityPort ownerEligibilityPort;

    // Mocked outbound port:
    // Registration service depends on agents-service to check agent eligibility.
    @MockitoBean
    private AgentEligibilityPort agentEligibilityPort;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated registration ID from the create response.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldTestRegistrationApiFullHappyPath() throws Exception {

        // ------------------- Step 1: Mock downstream eligibility checks -------------------
        // The registration-service normally calls other services:
        // cars-service, owners-service, and agents-service.
        // For this integration test, we mock those calls and return true.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-api-5001")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-api-5001")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-api-5001")).thenReturn(true);

        // Create future expiry dates for registration and renewal.
        // The domain rule requires expiry dates to be in the future.
        String expiry = LocalDate.now().plusYears(1).toString();
        String newExpiry = LocalDate.now().plusYears(2).toString();

        // ------------------- Step 2: Register vehicle -------------------
        // Send a POST request to create a new registration.
        // This simulates a client sending registration data to the API.
        MvcResult createResult = mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-api-5001",
                                  "ownerId": "owner-api-5001",
                                  "agentId": "agent-api-5001",
                                  "plate": "REG5001",
                                  "expiry": "%s"
                                }
                                """.formatted(expiry)))

                // The API should return 200 OK after creating the registration.
                .andExpect(status().isOk())

                // The response should contain a generated registration ID.
                .andExpect(jsonPath("$.id").exists())

                // The response should contain the same references that we sent.
                .andExpect(jsonPath("$.vehicleId").value("vehicle-api-5001"))
                .andExpect(jsonPath("$.ownerId").value("owner-api-5001"))
                .andExpect(jsonPath("$.agentId").value("agent-api-5001"))

                // The response should contain the normalized/stored plate number.
                .andExpect(jsonPath("$.plate").value("REG5001"))

                // Business rule:
                // A newly created registration should be ACTIVE.
                .andExpect(jsonPath("$.status").value("ACTIVE"))

                // Save the response so we can extract the generated ID.
                .andReturn();

        // Read the JSON response body as a String.
        String responseBody = createResult.getResponse().getContentAsString();

        // Convert the JSON String into a JsonNode object.
        JsonNode json = objectMapper.readTree(responseBody);

        // Extract the generated registration ID.
        // We need this ID for the next API calls.
        String registrationId = json.get("id").asText();

        // ------------------- Step 3: Get registration by ID -------------------
        // Send a GET request to find the registration we just created.
        mockMvc.perform(get("/api/registrations/{id}", registrationId))

                // The API should find the registration and return 200 OK.
                .andExpect(status().isOk())

                // The returned registration should match the created registration.
                .andExpect(jsonPath("$.id").value(registrationId))
                .andExpect(jsonPath("$.vehicleId").value("vehicle-api-5001"))
                .andExpect(jsonPath("$.ownerId").value("owner-api-5001"))
                .andExpect(jsonPath("$.agentId").value("agent-api-5001"))
                .andExpect(jsonPath("$.plate").value("REG5001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // ------------------- Step 4: List registrations -------------------
        // Send a GET request to retrieve all registrations.
        mockMvc.perform(get("/api/registrations"))

                // The API should return 200 OK.
                .andExpect(status().isOk());

        // ------------------- Step 5: Renew registration -------------------
        // Send a POST request to renew the registration with a new future expiry date.
        mockMvc.perform(post("/api/registrations/{id}/renew", registrationId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "newExpiry": "%s"
                                }
                                """.formatted(newExpiry)))

                // The API should return 200 OK after successful renewal.
                .andExpect(status().isOk())

                // The same registration ID should be returned.
                .andExpect(jsonPath("$.id").value(registrationId))

                // The expiry date should be updated to the new expiry date.
                .andExpect(jsonPath("$.expiry").value(newExpiry))

                // Renewing should keep the registration ACTIVE.
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // ------------------- Step 6: Cancel registration -------------------
        // Send a POST request to cancel the registration.
        mockMvc.perform(post("/api/registrations/{id}/cancel", registrationId))

                // The API should return 200 OK after successful cancellation.
                .andExpect(status().isOk())

                // The same registration ID should be returned.
                .andExpect(jsonPath("$.id").value(registrationId))

                // Business rule:
                // After cancellation, the registration status should be CANCELLED.
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // ------------------- Step 7: Delete registration -------------------
        // Send a DELETE request to remove the registration.
        mockMvc.perform(delete("/api/registrations/{id}", registrationId))

                // The API should return 204 No Content.
                // This means the delete operation succeeded and there is no response body.
                .andExpect(status().isNoContent());

        // ------------------- Step 8: Verify mocks were used -------------------
        // Verify that registration-service checked the vehicle eligibility.
        Mockito.verify(vehicleEligibilityPort).isEligible("vehicle-api-5001");

        // Verify that registration-service checked the owner eligibility.
        Mockito.verify(ownerEligibilityPort).isEligible("owner-api-5001");

        // Verify that registration-service checked the agent eligibility.
        Mockito.verify(agentEligibilityPort).isEligible("agent-api-5001");
    }
}