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

// @RestClientTest → loads ONLY REST client layer (not full Spring Boot)
// Used specifically for testing HTTP client adapters
@RestClientTest(OwnerEligibilityRestAdapter.class)

// Import test configuration for RestTemplate
@Import(OwnerEligibilityRestAdapterTest.TestRestTemplateConfig.class)

// Override base URL for owners-service in test
@TestPropertySource(properties = {
        "services.owners.base-url=http://localhost:9992"
})
class OwnerEligibilityRestAdapterTest {

    // Mock HTTP server → simulates external owners-service
    @Autowired
    private MockRestServiceServer mockServer;

    // Adapter under test (real object)
    @Autowired
    private OwnerEligibilityRestAdapter adapter;

    @Test
    @DisplayName("Should return true when owner is eligible")
    void shouldReturnTrueWhenOwnerIsEligible() {

        // ------------------- Arrange -------------------
        // Expect HTTP GET request to owners-service eligibility endpoint
        mockServer.expect(requestTo("http://localhost:9992/api/owners/owner-1/eligibility"))
                .andExpect(method(GET))

                // Simulate successful response: "true"
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        // ------------------- Act -------------------
        boolean result = adapter.isEligible("owner-1");

        // ------------------- Assert -------------------
        // Adapter should correctly interpret response
        assertThat(result).isTrue();

        // Verify HTTP interaction occurred as expected
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return false when owner is not eligible")
    void shouldReturnFalseWhenOwnerIsNotEligible() {

        // ------------------- Arrange -------------------
        mockServer.expect(requestTo("http://localhost:9992/api/owners/owner-2/eligibility"))
                .andExpect(method(GET))

                // Simulate successful response: "false"
                .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));

        // ------------------- Act -------------------
        boolean result = adapter.isEligible("owner-2");

        // ------------------- Assert -------------------
        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw exception when owners-service returns server error")
    void shouldThrowExceptionWhenOwnersServiceReturnsServerError() {

        // ------------------- Arrange -------------------
        mockServer.expect(requestTo("http://localhost:9992/api/owners/owner-3/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 500 error from external service
                .andRespond(withServerError());

        // ------------------- Act + Assert -------------------
        // Adapter should convert HTTP failure into domain/application exception
        assertThrows(CrossContextValidationException.class,
                () -> adapter.isEligible("owner-3"));

        mockServer.verify();
    }

    // Test configuration for RestTemplate
    // Provides required bean for adapter
    @TestConfiguration
    static class TestRestTemplateConfig {

        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            // Build RestTemplate used by adapter
            return builder.build();
        }
    }
}