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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Negative integration test → tests invalid API scenarios
// These tests make sure the API returns correct HTTP error codes
// Uses Spring Boot, MockMvc, and the "testing" profile
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
class RegistrationControllerNegativeIntegrationTest {

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
    // Here, we use it to extract the generated registration ID.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnBadRequestWhenPlateIsInvalid() throws Exception {

        // ------------------- Arrange -------------------
        // Mock downstream eligibility checks.
        // All related services say the vehicle, owner, and agent are eligible.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-neg-1")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-neg-1")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-neg-1")).thenReturn(true);

        // ------------------- Act + Assert -------------------
        // Send a POST request with an invalid plate number.
        // Business rule: plate number must have a valid length/format.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-1",
                                  "ownerId": "owner-neg-1",
                                  "agentId": "agent-neg-1",
                                  "plate": "A",
                                  "expiry": "%s"
                                }
                                """.formatted(LocalDate.now().plusYears(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenExpiryIsInPast() throws Exception {

        // ------------------- Arrange -------------------
        // Mock downstream eligibility checks as valid.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-neg-2")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-neg-2")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-neg-2")).thenReturn(true);

        // ------------------- Act + Assert -------------------
        // Send a POST request with an expiry date in the past.
        // Business rule: registration expiry must be a future date.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-2",
                                  "ownerId": "owner-neg-2",
                                  "agentId": "agent-neg-2",
                                  "plate": "NEG222",
                                  "expiry": "%s"
                                }
                                """.formatted(LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenPlateAlreadyExists() throws Exception {

        // ------------------- Arrange -------------------
        // Mock downstream eligibility checks as valid.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-neg-3")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-neg-3")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-neg-3")).thenReturn(true);

        // Use a valid future expiry date.
        String expiry = LocalDate.now().plusYears(1).toString();

        // First request creates the registration successfully.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-3",
                                  "ownerId": "owner-neg-3",
                                  "agentId": "agent-neg-3",
                                  "plate": "NEG333",
                                  "expiry": "%s"
                                }
                                """.formatted(expiry)))
                .andExpect(status().isOk());

        // ------------------- Act + Assert -------------------
        // Second request tries to create another registration with the same plate.
        // Business rule: two active registrations cannot use the same plate number.
        // Expected result: API returns 409 Conflict.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-3",
                                  "ownerId": "owner-neg-3",
                                  "agentId": "agent-neg-3",
                                  "plate": "NEG333",
                                  "expiry": "%s"
                                }
                                """.formatted(expiry)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenVehicleIsNotEligible() throws Exception {

        // ------------------- Arrange -------------------
        // Mock the vehicle as not eligible.
        // Owner and agent are eligible, so the failure is specifically caused by vehicle eligibility.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-neg-4")).thenReturn(false);
        Mockito.when(ownerEligibilityPort.isEligible("owner-neg-4")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-neg-4")).thenReturn(true);

        // ------------------- Act + Assert -------------------
        // Try to create a registration with an ineligible vehicle.
        // Business rule: registration cannot be created if the vehicle is not eligible.
        // Expected result: API returns 422 Unprocessable Entity.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-4",
                                  "ownerId": "owner-neg-4",
                                  "agentId": "agent-neg-4",
                                  "plate": "NEG444",
                                  "expiry": "%s"
                                }
                                """.formatted(LocalDate.now().plusYears(1))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenOwnerIsNotEligible() throws Exception {

        // ------------------- Arrange -------------------
        // Mock the owner as not eligible.
        // Vehicle and agent are eligible, so the failure is specifically caused by owner eligibility.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-neg-5")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-neg-5")).thenReturn(false);
        Mockito.when(agentEligibilityPort.isEligible("agent-neg-5")).thenReturn(true);

        // ------------------- Act + Assert -------------------
        // Try to create a registration with an ineligible owner.
        // Business rule: registration cannot be created if the owner is not eligible.
        // Expected result: API returns 422 Unprocessable Entity.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-5",
                                  "ownerId": "owner-neg-5",
                                  "agentId": "agent-neg-5",
                                  "plate": "NEG555",
                                  "expiry": "%s"
                                }
                                """.formatted(LocalDate.now().plusYears(1))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenAgentIsNotEligible() throws Exception {

        // ------------------- Arrange -------------------
        // Mock the agent as not eligible.
        // Vehicle and owner are eligible, so the failure is specifically caused by agent eligibility.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-neg-6")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-neg-6")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-neg-6")).thenReturn(false);

        // ------------------- Act + Assert -------------------
        // Try to create a registration with an ineligible agent.
        // Business rule: registration cannot be created if the agent is not eligible.
        // Expected result: API returns 422 Unprocessable Entity.
        mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": "vehicle-neg-6",
                                  "ownerId": "owner-neg-6",
                                  "agentId": "agent-neg-6",
                                  "plate": "NEG666",
                                  "expiry": "%s"
                                }
                                """.formatted(LocalDate.now().plusYears(1))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnNotFoundWhenRegistrationDoesNotExist() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to get a registration using an ID that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(get("/api/registrations/{id}", "missing-registration-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenRenewingMissingRegistration() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to renew a registration that does not exist.
        // The new expiry date is valid, but the registration ID is missing.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(post("/api/registrations/{id}/renew", "missing-registration-id")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "newExpiry": "%s"
                                }
                                """.formatted(LocalDate.now().plusYears(1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenRenewingExistingRegistrationWithPastExpiry() throws Exception {

        // ------------------- Arrange -------------------
        // Mock downstream eligibility checks as valid.
        // These mocks are needed because we first create a valid registration.
        Mockito.when(vehicleEligibilityPort.isEligible("vehicle-renew-1")).thenReturn(true);
        Mockito.when(ownerEligibilityPort.isEligible("owner-renew-1")).thenReturn(true);
        Mockito.when(agentEligibilityPort.isEligible("agent-renew-1")).thenReturn(true);

        // Create a valid future expiry date for the original registration.
        String expiry = LocalDate.now().plusYears(1).toString();

        // Create a valid registration first.
        // This gives us a real registration ID to use in the renewal test.
        MvcResult createResult = mockMvc.perform(post("/api/registrations")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "vehicleId": "vehicle-renew-1",
                              "ownerId": "owner-renew-1",
                              "agentId": "agent-renew-1",
                              "plate": "REN001",
                              "expiry": "%s"
                            }
                            """.formatted(expiry)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the generated registration ID from the create response.
        JsonNode json = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String registrationId = json.get("id").asText();

        // ------------------- Act + Assert -------------------
        // Try to renew the existing registration with a past expiry date.
        // Business rule: renewal expiry must be in the future.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/registrations/{id}/renew", registrationId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "newExpiry": "%s"
                            }
                            """.formatted(LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenCancellingMissingRegistration() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to cancel a registration that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(post("/api/registrations/{id}/cancel", "missing-registration-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingRegistration() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to delete a registration that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(delete("/api/registrations/{id}", "missing-registration-id"))
                .andExpect(status().isNotFound());
    }
}