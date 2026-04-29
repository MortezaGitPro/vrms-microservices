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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test → tests the API layer with Spring Boot
// Uses MockMvc to send fake HTTP requests to the controller
// Uses the "testing" profile, usually with an H2 database
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
class OwnerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated owner ID from the create response.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldTestOwnerApiFullHappyPath() throws Exception {

        // ------------------- Step 1: Create owner -------------------
        // Send a POST request to create a new owner.
        // This simulates a client sending JSON data to the API.
        MvcResult createResult = mockMvc.perform(post("/api/owners")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Owner API Full Path 5001",
                                  "address": "Montreal"
                                }
                                """))

                // The API should return 200 OK after creating the owner.
                .andExpect(status().isOk())

                // The response should contain a generated owner ID.
                .andExpect(jsonPath("$.id").exists())

                // The response should contain the same data that we sent.
                .andExpect(jsonPath("$.fullName").value("Owner API Full Path 5001"))
                .andExpect(jsonPath("$.address").value("Montreal"))

                // Save the response so we can extract the generated ID.
                .andReturn();

        // Read the JSON response body as a String.
        String responseBody = createResult.getResponse().getContentAsString();

        // Convert the JSON String into a JsonNode object.
        JsonNode json = objectMapper.readTree(responseBody);

        // Extract the generated owner ID.
        // We need this ID for the next API calls.
        String ownerId = json.get("id").asText();

        // ------------------- Step 2: Get owner by ID -------------------
        // Send a GET request to find the owner we just created.
        mockMvc.perform(get("/api/owners/{id}", ownerId))

                // The API should find the owner and return 200 OK.
                .andExpect(status().isOk())

                // The returned owner should match the created owner.
                .andExpect(jsonPath("$.id").value(ownerId))
                .andExpect(jsonPath("$.fullName").value("Owner API Full Path 5001"))
                .andExpect(jsonPath("$.address").value("Montreal"));

        // ------------------- Step 3: List owners -------------------
        // Send a GET request to retrieve all owners.
        mockMvc.perform(get("/api/owners"))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // At least one owner should exist in the list.
                // The first item should have an ID.
                .andExpect(jsonPath("$[0].id").exists());

        // ------------------- Step 4: Eligibility before activation -------------------
        // Send a GET request to check if the owner is eligible for registration.
        mockMvc.perform(get("/api/owners/{id}/eligibility", ownerId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // A newly created owner is INACTIVE, so they are not eligible yet.
                .andExpect(jsonPath("$").value(false));

        // ------------------- Step 5: Activate owner -------------------
        // Send a POST request to activate the owner.
        mockMvc.perform(post("/api/owners/{id}/activate", ownerId))

                // The API should return 200 OK after successful activation.
                .andExpect(status().isOk())

                // The response should still represent the same owner.
                .andExpect(jsonPath("$.id").value(ownerId))
                .andExpect(jsonPath("$.fullName").value("Owner API Full Path 5001"));

        // ------------------- Step 6: Eligibility after activation -------------------
        // Check eligibility again after activation.
        mockMvc.perform(get("/api/owners/{id}/eligibility", ownerId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // An ACTIVE owner is eligible for registration.
                .andExpect(jsonPath("$").value(true));

        // ------------------- Step 7: Suspend owner -------------------
        // Send a POST request to suspend the owner.
        mockMvc.perform(post("/api/owners/{id}/suspend", ownerId))

                // The API should return 200 OK after successful suspension.
                .andExpect(status().isOk())

                // The response should still represent the same owner.
                .andExpect(jsonPath("$.id").value(ownerId));

        // ------------------- Step 8: Eligibility after suspension -------------------
        // Check eligibility again after suspension.
        mockMvc.perform(get("/api/owners/{id}/eligibility", ownerId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // A SUSPENDED owner is not eligible for registration.
                .andExpect(jsonPath("$").value(false));

        // ------------------- Step 9: Update owner -------------------
        // Send a PUT request to update the owner's full name and address.
        mockMvc.perform(put("/api/owners/{id}", ownerId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Owner API Full Path 5002",
                                  "address": "Laval"
                                }
                                """))

                // The API should return 200 OK after the update.
                .andExpect(status().isOk())

                // The ID should stay the same.
                .andExpect(jsonPath("$.id").value(ownerId))

                // The owner data should be updated.
                .andExpect(jsonPath("$.fullName").value("Owner API Full Path 5002"))
                .andExpect(jsonPath("$.address").value("Laval"));

        // ------------------- Step 10: Delete owner -------------------
        // Send a DELETE request to remove the owner.
        mockMvc.perform(delete("/api/owners/{id}", ownerId))

                // The API should return 204 No Content.
                // This means the delete operation succeeded and there is no response body.
                .andExpect(status().isNoContent());
    }
}