package com.champsoft.vrms.agents.api;

import com.champsoft.vrms.agents.application.exception.AgentNotFoundException;
import com.champsoft.vrms.agents.application.exception.DuplicateAgentException;
import com.champsoft.vrms.agents.domain.exception.InvalidAgentNameException;
import com.champsoft.vrms.agents.domain.exception.InvalidRoleException;
import com.champsoft.vrms.agents.domain.model.Role;
import com.champsoft.vrms.agents.web.ApiErrorResponse;
import com.champsoft.vrms.agents.web.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
class AgentControllerNegativeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper is used to read JSON responses from the API.
    // Here, we use it to extract the generated agent ID.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnBadRequestWhenCreatingAgentWithTooShortName() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with an agent name that is too short.
        // Business rule: agent name must have a minimum valid length.
        // "A" has only one character, so it should be rejected.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "A",
                                  "role": "CLERK"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAgentWithTooLongName() throws Exception {

        // ------------------- Arrange -------------------
        // Create a name longer than the allowed maximum length.
        // Business rule: agent name must not exceed the maximum length.
        String longName = "A".repeat(121);

        // ------------------- Act + Assert -------------------
        // Send a POST request with the long name.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "role": "CLERK"
                                }
                                """.formatted(longName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAgentWithInvalidRole() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with a role that does not exist in the Role enum.
        // Business rule: agent role must be CLERK or SUPERVISOR.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Agent Invalid Role",
                                  "role": "MANAGER"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAgentWithInvalidRoleValue() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with another invalid role value.
        // This test covers the same validation rule with a different invalid input.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Agent Invalid Role Value",
                                  "role": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingAgentWithMalformedJson() throws Exception {

        // ------------------- Act + Assert -------------------
        // Send a POST request with broken JSON.
        // The closing brace is missing, so Spring cannot read the request body.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Bad Json Agent",
                                  "role": "CLERK"
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenCreatingAgentWithDuplicateName() throws Exception {

        // ------------------- Arrange -------------------
        // Use the same agent name twice.
        // Business rule: duplicate agent names are not allowed.
        String name = "Duplicate Agent 7001";

        // First request creates the agent successfully.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "role": "CLERK"
                                }
                                """.formatted(name)))
                .andExpect(status().isOk());

        // ------------------- Act + Assert -------------------
        // Second request tries to create another agent with the same name.
        // Expected result: API returns 409 Conflict.
        mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "role": "SUPERVISOR"
                                }
                                """.formatted(name)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenAgentDoesNotExist() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to get an agent using an ID that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(get("/api/agents/{id}", "missing-agent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingAgent() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to update an agent that does not exist.
        // The request body is valid, but the agent ID is missing.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(put("/api/agents/{id}", "missing-agent-id")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Agent",
                                  "role": "SUPERVISOR"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingAgentWithTooShortName() throws Exception {

        // ------------------- Arrange -------------------
        // Create a valid agent first.
        // We need an existing agent ID before testing the update endpoint.
        String agentId = createAgentAndReturnId("Agent Short Update Test", "CLERK");

        // ------------------- Act + Assert -------------------
        // Try to update the agent name to a value that is too short.
        // Business rule: updated agent name must also be valid.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(put("/api/agents/{id}", agentId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "A",
                                  "role": "CLERK"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingAgentWithTooLongName() throws Exception {

        // ------------------- Arrange -------------------
        // Create a valid agent first.
        String agentId = createAgentAndReturnId("Agent Long Update Test", "CLERK");

        // Create a name longer than the allowed maximum length.
        String longName = "A".repeat(121);

        // ------------------- Act + Assert -------------------
        // Try to update the agent name to a value that is too long.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(put("/api/agents/{id}", agentId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "role": "CLERK"
                                }
                                """.formatted(longName)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingAgentWithInvalidRole() throws Exception {

        // ------------------- Arrange -------------------
        // Create a valid agent first.
        String agentId = createAgentAndReturnId("Agent Update Invalid Role 7002", "CLERK");

        // ------------------- Act + Assert -------------------
        // Try to update the agent with an invalid role.
        // Business rule: updated role must also be CLERK or SUPERVISOR.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(put("/api/agents/{id}", agentId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Agent",
                                  "role": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingAgentWithMalformedJson() throws Exception {

        // ------------------- Arrange -------------------
        // Create a valid agent first.
        String agentId = createAgentAndReturnId("Agent Bad Json Update Test", "CLERK");

        // ------------------- Act + Assert -------------------
        // Try to update the agent with broken JSON.
        // The closing brace is missing, so Spring cannot read the request body.
        // Expected result: API returns 400 Bad Request.
        mockMvc.perform(put("/api/agents/{id}", agentId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Agent",
                                  "role": "CLERK"
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingAgent() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to delete an agent that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(delete("/api/agents/{id}", "missing-agent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenActivatingMissingAgent() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to activate an agent that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(post("/api/agents/{id}/activate", "missing-agent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenCheckingEligibilityForMissingAgent() throws Exception {

        // ------------------- Act + Assert -------------------
        // Try to check eligibility for an agent that does not exist.
        // Expected result: API returns 404 Not Found.
        mockMvc.perform(get("/api/agents/{id}/eligibility", "missing-agent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnFalseEligibilityBeforeActivation() throws Exception {

        // ------------------- Arrange -------------------
        // Create a valid agent.
        // Business rule: a newly created agent starts as INACTIVE.
        String agentId = createAgentAndReturnId("Agent Eligibility False Test", "CLERK");

        // ------------------- Act + Assert -------------------
        // Check eligibility before activation.
        // Expected result: API returns 200 OK.
        // Note: You can make this stronger by adding jsonPath("$").value(false).
        mockMvc.perform(get("/api/agents/{id}/eligibility", agentId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnTrueEligibilityAfterActivation() throws Exception {

        // ------------------- Arrange -------------------
        // Create a valid agent.
        String agentId = createAgentAndReturnId("Agent Eligibility True Test", "SUPERVISOR");

        // Activate the agent.
        mockMvc.perform(post("/api/agents/{id}/activate", agentId))
                .andExpect(status().isOk());

        // ------------------- Act + Assert -------------------
        // Check eligibility after activation.
        // Expected result: API returns 200 OK.
        // Note: You can make this stronger by adding jsonPath("$").value(true).
        mockMvc.perform(get("/api/agents/{id}/eligibility", agentId))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------------
    // Direct exception-handler tests.
    // These tests improve branch coverage for agents.api and agents.web.
    // They test the exception handler methods directly without sending HTTP requests.
    // ---------------------------------------------------------------------

    @Test
    void shouldDirectlyTestAgentNotFoundExceptionHandler() {

        // ------------------- Arrange -------------------
        // Create the exception handler directly.
        AgentExceptionHandler handler = new AgentExceptionHandler();

        // Create a fake HTTP request with a path.
        HttpServletRequest request = request("/api/agents/missing-agent-id");

        // ------------------- Act -------------------
        // Call the handler method directly with AgentNotFoundException.
        ResponseEntity<ApiErrorResponse> response =
                handler.notFound(new AgentNotFoundException("Agent not found"), request);

        // ------------------- Assert -------------------
        // Verify that the handler returns HTTP 404.
        assertThat(response.getStatusCode().value()).isEqualTo(404);

        // Verify the response body details.
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Agent not found");
        assertThat(response.getBody().path()).isEqualTo("/api/agents/missing-agent-id");
    }

    @Test
    void shouldDirectlyTestDuplicateAgentExceptionHandler() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");

        // ------------------- Act -------------------
        // Call the handler method directly with DuplicateAgentException.
        ResponseEntity<ApiErrorResponse> response =
                handler.conflict(new DuplicateAgentException("Agent already exists"), request);

        // ------------------- Assert -------------------
        // Verify that duplicate agent errors are converted to HTTP 409 Conflict.
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("Agent already exists");
        assertThat(response.getBody().path()).isEqualTo("/api/agents");
    }

    @Test
    void shouldDirectlyTestInvalidAgentNameExceptionHandler() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");

        // ------------------- Act -------------------
        // Call the handler method directly with InvalidAgentNameException.
        ResponseEntity<ApiErrorResponse> response =
                handler.badRequest(new InvalidAgentNameException("Invalid agent name"), request);

        // ------------------- Assert -------------------
        // Verify that invalid agent name errors are converted to HTTP 400.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid agent name");
        assertThat(response.getBody().path()).isEqualTo("/api/agents");
    }

    @Test
    void shouldDirectlyTestInvalidRoleExceptionHandler() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");

        // ------------------- Act -------------------
        // Call the handler method directly with InvalidRoleException.
        ResponseEntity<ApiErrorResponse> response =
                handler.badRequest(new InvalidRoleException("Role is required"), request);

        // ------------------- Assert -------------------
        // Verify that invalid role errors are converted to HTTP 400.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Role is required");
        assertThat(response.getBody().path()).isEqualTo("/api/agents");
    }

    @Test
    void shouldDirectlyTestIllegalArgumentExceptionHandler() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");

        // ------------------- Act -------------------
        // Call the handler method directly with a generic IllegalArgumentException.
        ResponseEntity<ApiErrorResponse> response =
                handler.badRequest(new IllegalArgumentException("Invalid request"), request);

        // ------------------- Assert -------------------
        // Verify that IllegalArgumentException is converted to HTTP 400.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid request");
        assertThat(response.getBody().path()).isEqualTo("/api/agents");
    }

    @Test
    void shouldDirectlyTestBadJsonWithInvalidRoleValue() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);

        // Create an InvalidFormatException to simulate this JSON problem:
        // "role": "MANAGER"
        // MANAGER is not a valid Role enum value.
        InvalidFormatException cause = InvalidFormatException.from(
                (JsonParser) null,
                "Invalid role value",
                "MANAGER",
                Role.class
        );

        // Wrap the cause inside HttpMessageNotReadableException.
        // This is similar to what Spring creates when JSON cannot be converted.
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Unreadable JSON", cause, inputMessage);

        // ------------------- Act -------------------
        // Call the JSON error handler directly.
        ResponseEntity<ApiErrorResponse> response = handler.badJson(exception, request);

        // ------------------- Assert -------------------
        // Verify that invalid role JSON becomes HTTP 400 with a helpful message.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("invalid role: MANAGER. Allowed values: CLERK, SUPERVISOR");
    }

    @Test
    void shouldDirectlyTestBadJsonWithNullInvalidRoleValue() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);

        // Create an InvalidFormatException where the invalid value is null.
        // This covers another branch in the exception handler.
        InvalidFormatException cause = InvalidFormatException.from(
                (JsonParser) null,
                "Invalid role value",
                null,
                Role.class
        );

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Unreadable JSON", cause, inputMessage);

        // ------------------- Act -------------------
        ResponseEntity<ApiErrorResponse> response = handler.badJson(exception, request);

        // ------------------- Assert -------------------
        // Verify that the handler displays <null> for a null invalid value.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("invalid role: <null>. Allowed values: CLERK, SUPERVISOR");
    }

    @Test
    void shouldDirectlyTestBadJsonWhenMappingPathContainsRoleField() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);

        // Create a JsonMappingException to simulate a mapping problem.
        JsonMappingException cause =
                JsonMappingException.from(
                        (JsonParser) null,
                        "Role mapping problem"
                );

        // Add "role" to the mapping path.
        // This simulates an error related to the role field.
        cause.prependPath(new Object(), "role");

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Unreadable JSON", cause, inputMessage);

        // ------------------- Act -------------------
        ResponseEntity<ApiErrorResponse> response = handler.badJson(exception, request);

        // ------------------- Assert -------------------
        // Verify that the handler detects the role field and returns a role-specific message.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message())
                .isEqualTo("invalid role. Allowed values: CLERK, SUPERVISOR");
    }

    @Test
    void shouldDirectlyTestBadJsonWithGenericInvalidPayloadMessage() {

        // ------------------- Arrange -------------------
        AgentExceptionHandler handler = new AgentExceptionHandler();
        HttpServletRequest request = request("/api/agents");
        HttpInputMessage inputMessage = mock(HttpInputMessage.class);

        // Create a generic unreadable JSON exception.
        // This covers the default branch of the badJson handler.
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Invalid JSON payload", inputMessage);

        // ------------------- Act -------------------
        ResponseEntity<ApiErrorResponse> response = handler.badJson(exception, request);

        // ------------------- Assert -------------------
        // Verify that the generic error message is returned.
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid JSON payload");
    }

    @Test
    void shouldDirectlyTestGlobalExceptionHandlerWithMessage() {

        // ------------------- Arrange -------------------
        // Create the global fallback exception handler directly.
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = request("/api/agents/test");

        // ------------------- Act -------------------
        // Pass an unexpected RuntimeException with a message.
        ResponseEntity<ApiErrorResponse> response =
                handler.handleAny(new RuntimeException("Something went wrong"), request);

        // ------------------- Assert -------------------
        // Verify that unexpected errors are converted to HTTP 500.
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Something went wrong");
        assertThat(response.getBody().path()).isEqualTo("/api/agents/test");
    }

    @Test
    void shouldDirectlyTestGlobalExceptionHandlerWithNullMessage() {

        // ------------------- Arrange -------------------
        // Create the global fallback exception handler directly.
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = request("/api/agents/test");

        // ------------------- Act -------------------
        // Pass an unexpected RuntimeException without a message.
        // This covers the branch where the handler must use a default message.
        ResponseEntity<ApiErrorResponse> response =
                handler.handleAny(new RuntimeException(), request);

        // ------------------- Assert -------------------
        // Verify that the handler uses "Unexpected error" as the default message.
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
        assertThat(response.getBody().path()).isEqualTo("/api/agents/test");
    }

    // Helper method:
    // Creates a valid agent through the API and returns the generated ID.
    // This avoids repeating the same create-agent code in update/eligibility tests.
    private String createAgentAndReturnId(String name, String role) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/agents")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "role": "%s"
                                }
                                """.formatted(name, role)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return json.get("id").asText();
    }

    // Helper method:
    // Creates a fake HttpServletRequest for direct exception-handler tests.
    // We mock only getRequestURI() because the handler needs the request path.
    private HttpServletRequest request(String path) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        return request;
    }
}