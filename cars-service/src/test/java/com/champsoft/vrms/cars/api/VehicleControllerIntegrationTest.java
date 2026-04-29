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
class VehicleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated vehicle ID from the create response.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldTestVehicleApiFullHappyPath() throws Exception {

        // ------------------- Step 1: Create vehicle -------------------
        // Send a POST request to create a new vehicle.
        // This simulates a client sending JSON data to the API.
        MvcResult createResult = mockMvc.perform(post("/api/cars")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "vin": "9HGCM82633A555555",
                                  "make": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020
                                }
                                """))

                // The API should return 200 OK after creating the vehicle.
                .andExpect(status().isOk())

                // The response should contain a generated vehicle ID.
                .andExpect(jsonPath("$.id").exists())

                // The response should contain the same data that we sent.
                .andExpect(jsonPath("$.vin").value("9HGCM82633A555555"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.year").value(2020))

                // Save the response so we can extract the generated ID.
                .andReturn();

        // Read the JSON response body as a String.
        String responseBody = createResult.getResponse().getContentAsString();

        // Convert the JSON String into a JsonNode object.
        JsonNode json = objectMapper.readTree(responseBody);

        // Extract the generated vehicle ID.
        // We need this ID for the next API calls.
        String vehicleId = json.get("id").asText();

        // ------------------- Step 2: Get vehicle by ID -------------------
        // Send a GET request to find the vehicle we just created.
        mockMvc.perform(get("/api/cars/{id}", vehicleId))

                // The API should find the vehicle and return 200 OK.
                .andExpect(status().isOk())

                // The returned vehicle should match the created vehicle.
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("9HGCM82633A555555"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.year").value(2020));

        // ------------------- Step 3: List vehicles -------------------
        // Send a GET request to retrieve all vehicles.
        mockMvc.perform(get("/api/cars"))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // At least one vehicle should exist in the list.
                // The first item should have an ID.
                .andExpect(jsonPath("$[0].id").exists());

        // ------------------- Step 4: Check eligibility before activation -------------------
        // Send a GET request to check if the vehicle is eligible for registration.
        mockMvc.perform(get("/api/cars/{id}/eligibility", vehicleId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // A newly created vehicle is INACTIVE, so it is not eligible yet.
                .andExpect(jsonPath("$").value(false));

        // ------------------- Step 5: Activate vehicle -------------------
        // Send a POST request to activate the vehicle.
        mockMvc.perform(post("/api/cars/{id}/activate", vehicleId))

                // The API should return 200 OK after successful activation.
                .andExpect(status().isOk())

                // The response should still represent the same vehicle.
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("9HGCM82633A555555"));

        // ------------------- Step 6: Check eligibility after activation -------------------
        // Check eligibility again after activation.
        mockMvc.perform(get("/api/cars/{id}/eligibility", vehicleId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // An ACTIVE vehicle is eligible for registration.
                .andExpect(jsonPath("$").value(true));

        // ------------------- Step 7: Update vehicle specs -------------------
        // Send a PUT request to update the vehicle make, model, and year.
        // Notice that the VIN is not changed here.
        mockMvc.perform(put("/api/cars/{id}", vehicleId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "make": "Honda",
                                  "model": "Civic",
                                  "year": 2022
                                }
                                """))

                // The API should return 200 OK after the update.
                .andExpect(status().isOk())

                // The ID and VIN should stay the same.
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.vin").value("9HGCM82633A555555"))

                // The specs should be updated.
                .andExpect(jsonPath("$.make").value("Honda"))
                .andExpect(jsonPath("$.model").value("Civic"))
                .andExpect(jsonPath("$.year").value(2022));

        // ------------------- Step 8: Delete vehicle -------------------
        // Send a DELETE request to remove the vehicle.
        mockMvc.perform(delete("/api/cars/{id}", vehicleId))

                // The API should return 204 No Content.
                // This means the delete operation succeeded and there is no response body.
                .andExpect(status().isNoContent());
    }
}