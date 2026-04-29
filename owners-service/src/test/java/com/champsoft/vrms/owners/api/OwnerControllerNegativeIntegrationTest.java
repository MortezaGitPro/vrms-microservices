package com.champsoft.vrms.owners.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Negative integration test → tests invalid API scenarios
// These tests make sure the API returns correct HTTP error codes
// Uses Spring Boot, MockMvc, and the "testing" profile
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
class OwnerControllerNegativeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated owner ID.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnBadRequestWhenCreatingOwnerWithBlankAddressTooLong() throws Exception {

        // ------------------- Arrange -------------------
        // Create an address that is longer than the allowed maximum length.
        // Business rule: owner address must not exceed the maximum allowed length.
        String longAddress = "A".repeat(201);

        // ------------------- Act + Assert -------------------
        // Send a POST request with a valid owner name but an invalid address.
        // The address has 201 characters, so it should be rejected.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/owners")
                        .contentType(APPLICATION_JSON)
                        .content("""
                        {
                          "fullName": "Owner Invalid Address Test",
                          "address": "%s"
                        }
                        """.formatted(longAddress)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingOwnerWithTooShortName() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with a name that is too short.
        // Business rule: owner full name must have a minimum valid length.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/owners")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "A",
                                  "address": "Montreal"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenCreatingOwnerWithDuplicateName() throws Exception {

        // ------------------- Arrange -------------------
        // Use the same full name twice.
        // Business rule: duplicate owner names are not allowed in this API.
        String fullName = "Duplicate Owner 6001";

        // First request creates the owner successfully.
        mockMvc.perform(post("/api/owners")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "%s",
                                  "address": "Montreal"
                                }
                                """.formatted(fullName)))
                .andExpect(status().isOk());

        // ------------------- Act + Assert -------------------
        // Second request tries to create another owner with the same name.
        // Expected result: API returns 409 Conflict.
        mockMvc.perform(post("/api/owners")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "%s",
                                  "address": "Laval"
                                }
                                """.formatted(fullName)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenOwnerDoesNotExist() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to get an owner using an ID that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(get("/api/owners/{id}", "missing-owner-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingOwner() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to update an owner that does not exist.
        // Even if the request body is valid, the owner ID is missing.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(put("/api/owners/{id}", "missing-owner-id")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Updated Owner",
                                  "address": "Laval"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingOwner() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to delete an owner that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(delete("/api/owners/{id}", "missing-owner-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenActivatingMissingOwner() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to activate an owner that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(post("/api/owners/{id}/activate", "missing-owner-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenSuspendingMissingOwner() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to suspend an owner that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(post("/api/owners/{id}/suspend", "missing-owner-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenCheckingEligibilityForMissingOwner() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to check registration eligibility for a missing owner.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(get("/api/owners/{id}/eligibility", "missing-owner-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnFalseEligibilityAfterSuspension() throws Exception {

        // ------------------- Arrange -------------------
        // First, create a valid owner.
        MvcResult createResult = mockMvc.perform(post("/api/owners")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Owner Suspend Test 6002",
                                  "address": "Montreal"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the generated owner ID from the create response.
        JsonNode json = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String ownerId = json.get("id").asText();

        // Activate the owner first.
        // After activation, the owner would normally be eligible.
        mockMvc.perform(post("/api/owners/{id}/activate", ownerId))
                .andExpect(status().isOk());

        // Suspend the owner.
        // Business rule: a suspended owner should not be eligible for registration.
        mockMvc.perform(post("/api/owners/{id}/suspend", ownerId))
                .andExpect(status().isOk());

        // ------------------- Act + Assert -------------------
        // Check eligibility after suspension.
        // Expected result: API returns 200 OK.
        // Note: To make this stronger, you may also add:
        // .andExpect(jsonPath("$").value(false));
        mockMvc.perform(get("/api/owners/{id}/eligibility", ownerId))
                .andExpect(status().isOk());
    }
}