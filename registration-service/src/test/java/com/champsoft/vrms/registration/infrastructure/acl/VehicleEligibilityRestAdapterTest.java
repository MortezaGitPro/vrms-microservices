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

// @RestClientTest → loads ONLY REST client layer (NOT full Spring Boot)
// Used for testing HTTP clients like RestTemplate adapters
@RestClientTest(VehicleEligibilityRestAdapter.class)

// Import custom configuration (RestTemplate bean)
@Import(VehicleEligibilityRestAdapterTest.TestRestTemplateConfig.class)

// Override properties for test environment
@TestPropertySource(properties = {
        "services.cars.base-url=http://localhost:9991"
})
class VehicleEligibilityRestAdapterTest {

    // Mock HTTP server → simulates external microservice (cars-service)
    @Autowired
    private MockRestServiceServer mockServer;

    // Adapter under test (real object)
    @Autowired
    private VehicleEligibilityRestAdapter adapter;

    @Test
    @DisplayName("Should return true when vehicle is eligible")
    void shouldReturnTrueWhenVehicleIsEligible() {

        // ------------------- Arrange -------------------
        // Expect a GET request to cars-service eligibility endpoint
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-1/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 200 response with body "true"
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        // ------------------- Act -------------------
        boolean result = adapter.isEligible("vehicle-1");

        // ------------------- Assert -------------------
        // Adapter should correctly parse response
        assertThat(result).isTrue();

        // Verify that expected HTTP call was executed
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return false when vehicle is not eligible")
    void shouldReturnFalseWhenVehicleIsNotEligible() {

        // ------------------- Arrange -------------------
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-2/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 200 response with "false"
                .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));

        // ------------------- Act -------------------
        boolean result = adapter.isEligible("vehicle-2");

        // ------------------- Assert -------------------
        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw exception when cars-service returns server error")
    void shouldThrowExceptionWhenCarsServiceReturnsServerError() {

        // ------------------- Arrange -------------------
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-3/eligibility"))
                .andExpect(method(GET))

                // Simulate HTTP 500 error
                .andRespond(withServerError());

        // ------------------- Act + Assert -------------------
        // Adapter should translate HTTP error into domain/application exception
        assertThrows(CrossContextValidationException.class,
                () -> adapter.isEligible("vehicle-3"));

        mockServer.verify();
    }

    // Test-specific configuration
    // Provides RestTemplate bean required by adapter
    @TestConfiguration
    static class TestRestTemplateConfig {

        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            // Build RestTemplate used by adapter
            return builder.build();
        }
    }
}