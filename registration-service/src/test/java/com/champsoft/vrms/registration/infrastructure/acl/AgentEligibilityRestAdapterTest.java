package com.champsoft.vrms.registration.infrastructure.acl;

import com.champsoft.vrms.registration.application.exception.CrossContextValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// @RestClientTest → loads ONLY the REST client layer
// We are testing the Adapter (HTTP client), NOT the full application
@RestClientTest(AgentEligibilityRestAdapter.class)

// Import custom configuration for RestTemplate
@Import(AgentEligibilityRestAdapterTest.TestRestTemplateConfig.class)

// Override base URL for agents-service in test environment
@TestPropertySource(properties = {
        "services.agents.base-url=http://localhost:9993"
})
class AgentEligibilityRestAdapterTest {

    // Mock HTTP server → simulates the external agents-service
    @Autowired
    private MockRestServiceServer mockServer;

    // Real adapter under test
    @Autowired
    private AgentEligibilityRestAdapter adapter;

    @Test
    @DisplayName("Should return true when agent is eligible")
    void shouldReturnTrueWhenAgentIsEligible() {

        // ------------------- Arrange -------------------
        // Expect a GET request to agents-service eligibility endpoint
        mockServer.expect(requestTo("http://localhost:9993/api/agents/agent-1/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 200 response with body "true"
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        // ------------------- Act -------------------
        boolean result = adapter.isEligible("agent-1");

        // ------------------- Assert -------------------
        // Adapter should correctly parse "true"
        assertThat(result).isTrue();

        // Verify HTTP interaction happened as expected
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return false when agent is not eligible")
    void shouldReturnFalseWhenAgentIsNotEligible() {

        // ------------------- Arrange -------------------
        mockServer.expect(requestTo("http://localhost:9993/api/agents/agent-2/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 200 response with "false"
                .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));

        // ------------------- Act -------------------
        boolean result = adapter.isEligible("agent-2");

        // ------------------- Assert -------------------
        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw exception when agents-service returns server error")
    void shouldThrowExceptionWhenAgentsServiceReturnsServerError() {

        // ------------------- Arrange -------------------
        mockServer.expect(requestTo("http://localhost:9993/api/agents/agent-3/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 500 error from external service
                .andRespond(withServerError());

        // ------------------- Act + Assert -------------------
        // Adapter should convert HTTP error into domain/application exception
        assertThrows(CrossContextValidationException.class,
                () -> adapter.isEligible("agent-3"));

        // Verify interaction
        mockServer.verify();
    }

    // Test-specific configuration
    // Provides RestTemplate bean required by the adapter
    @TestConfiguration
    static class TestRestTemplateConfig {

        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            // Build RestTemplate used by adapter
            return builder.build();
        }
    }
}