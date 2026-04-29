package com.champsoft.vrms.cars.api;

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
class VehicleControllerNegativeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated vehicle ID.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnBadRequestWhenCreatingVehicleWithInvalidVin() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with an invalid VIN.
        // Business rule: VIN must follow the required format/length.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/cars")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": "BAD",
                                  "make": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingVehicleWithInvalidYear() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with an invalid vehicle year.
        // Business rule: vehicle year must be inside the allowed range.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/cars")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": "1HGCM82633A123999",
                                  "make": "Toyota",
                                  "model": "Corolla",
                                  "year": 1970
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenCreatingVehicleWithDuplicateVin() throws Exception {

        // ------------------- Arrange -------------------
        // Use one VIN value twice.
        // Business rule: two vehicles cannot have the same VIN.
        String vin = "1HGCM82633A777777";

        // First request creates the vehicle successfully.
        mockMvc.perform(post("/api/cars")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": "%s",
                                  "make": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020
                                }
                                """.formatted(vin)))
                .andExpect(status().isOk());

        // ------------------- Act + Assert -------------------
        // Second request tries to create another vehicle with the same VIN.
        // Expected result: API returns 409 Conflict.
        mockMvc.perform(post("/api/cars")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": "%s",
                                  "make": "Honda",
                                  "model": "Civic",
                                  "year": 2022
                                }
                                """.formatted(vin)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenVehicleDoesNotExist() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to get a vehicle using an ID that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(get("/api/cars/{id}", "missing-car-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingVehicle() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to update a vehicle that does not exist.
        // Even if the request body is valid, the vehicle ID is missing.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(put("/api/cars/{id}", "missing-car-id")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "make": "Honda",
                                  "model": "Civic",
                                  "year": 2022
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenActivatingAlreadyActiveVehicle() throws Exception {

        // Arrange
        // Create a new vehicle. New vehicles start as INACTIVE.
        MvcResult createResult = mockMvc.perform(post("/api/cars")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "vin": "1HGCM82633A888888",
                              "make": "Mazda",
                              "model": "Mazda3",
                              "year": 2021
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        // Extract the generated vehicle id from the JSON response.
        JsonNode json = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String vehicleId = json.get("id").asText();

        // Act 1
        // First activation should succeed because the vehicle is inactive.
        mockMvc.perform(post("/api/cars/{id}/activate", vehicleId))
                .andExpect(status().isOk());

        // Act 2 + Assert
        // Second activation should fail because the vehicle is already active.
        // In the current project, VehicleAlreadyActiveException is mapped to 400 Bad Request.
        mockMvc.perform(post("/api/cars/{id}/activate", vehicleId))
                .andExpect(status().isBadRequest());
    }
}