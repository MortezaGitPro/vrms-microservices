package com.champsoft.vrms.agents.api;

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
class AgentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated agent ID from the create response.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldTestAgentApiFullHappyPath() throws Exception {

        // ------------------- Step 1: Create agent -------------------
        // Send a POST request to create a new agent.
        // This simulates a client sending JSON data to the API.
        MvcResult createResult = mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Agent API Full Path 5001",
                                  "role": "CLERK"
                                }
                                """))

                // The API should return 200 OK after creating the agent.
                .andExpect(status().isOk())

                // The response should contain a generated agent ID.
                .andExpect(jsonPath("$.id").exists())

                // The response should contain the same data that we sent.
                .andExpect(jsonPath("$.name").value("Agent API Full Path 5001"))
                .andExpect(jsonPath("$.role").value("CLERK"))

                // Save the response so we can extract the generated ID.
                .andReturn();

        // Read the JSON response body as a String.
        String responseBody = createResult.getResponse().getContentAsString();

        // Convert the JSON String into a JsonNode object.
        JsonNode json = objectMapper.readTree(responseBody);

        // Extract the generated agent ID.
        // We need this ID for the next API calls.
        String agentId = json.get("id").asText();

        // ------------------- Step 2: Get agent by ID -------------------
        // Send a GET request to find the agent we just created.
        mockMvc.perform(get("/api/agents/{id}", agentId))

                // The API should find the agent and return 200 OK.
                .andExpect(status().isOk())

                // The returned agent should match the created agent.
                .andExpect(jsonPath("$.id").value(agentId))
                .andExpect(jsonPath("$.name").value("Agent API Full Path 5001"))
                .andExpect(jsonPath("$.role").value("CLERK"));

        // ------------------- Step 3: List agents -------------------
        // Send a GET request to retrieve all agents.
        mockMvc.perform(get("/api/agents"))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // At least one agent should exist in the list.
                // The first item should have an ID.
                .andExpect(jsonPath("$[0].id").exists());

        // ------------------- Step 4: Eligibility before activation -------------------
        // Send a GET request to check if the agent is eligible to process registrations.
        mockMvc.perform(get("/api/agents/{id}/eligibility", agentId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // A newly created agent is INACTIVE, so they are not eligible yet.
                .andExpect(jsonPath("$").value(false));

        // ------------------- Step 5: Activate agent -------------------
        // Send a POST request to activate the agent.
        mockMvc.perform(post("/api/agents/{id}/activate", agentId))

                // The API should return 200 OK after successful activation.
                .andExpect(status().isOk())

                // The response should still represent the same agent.
                .andExpect(jsonPath("$.id").value(agentId))
                .andExpect(jsonPath("$.name").value("Agent API Full Path 5001"));

        // ------------------- Step 6: Eligibility after activation -------------------
        // Check eligibility again after activation.
        mockMvc.perform(get("/api/agents/{id}/eligibility", agentId))

                // The API should return 200 OK.
                .andExpect(status().isOk())

                // Business rule:
                // An ACTIVE agent is eligible to process registrations.
                .andExpect(jsonPath("$").value(true));

        // ------------------- Step 7: Update agent -------------------
        // Send a PUT request to update the agent's name and role.
        mockMvc.perform(put("/api/agents/{id}", agentId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Agent API Full Path 5002",
                                  "role": "SUPERVISOR"
                                }
                                """))

                // The API should return 200 OK after the update.
                .andExpect(status().isOk())

                // The ID should stay the same.
                .andExpect(jsonPath("$.id").value(agentId))

                // The agent data should be updated.
                .andExpect(jsonPath("$.name").value("Agent API Full Path 5002"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"));

        // ------------------- Step 8: Delete agent -------------------
        // Send a DELETE request to remove the agent.
        mockMvc.perform(delete("/api/agents/{id}", agentId))

                // The API should return 204 No Content.
                // This means the delete operation succeeded and there is no response body.
                .andExpect(status().isNoContent());
    }
}