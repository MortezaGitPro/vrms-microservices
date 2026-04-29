package com.champsoft.vrms.registration.infrastructure.client;

import com.champsoft.vrms.registration.infrastructure.acl.VehicleEligibilityRestAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

@RestClientTest
@Import(VehicleEligibilityRestAdapter.class)
@ActiveProfiles("testing")
class VehicleEligibilityRestAdapterTest {

    // Fake HTTP server used to test outbound REST calls.
    @Autowired
    private MockRestServiceServer mockServer;

    // Real adapter under test.
    @Autowired
    private VehicleEligibilityRestAdapter adapter;

    @Test
    @DisplayName("Should return true when cars-service says vehicle is eligible")
    void shouldReturnTrueWhenVehicleIsEligible() {
        // Arrange
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-1/eligibility"))
                .andExpect(method(GET))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        // Act
        boolean result = adapter.isEligible("vehicle-1");

        // Assert
        assertThat(result).isTrue();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return false when cars-service says vehicle is not eligible")
    void shouldReturnFalseWhenVehicleIsNotEligible() {
        // Arrange
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-2/eligibility"))
                .andExpect(method(GET))
                .andRespond(withSuccess("false", MediaType.APPLICATION_JSON));

        // Act
        boolean result = adapter.isEligible("vehicle-2");

        // Assert
        assertThat(result).isFalse();
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw exception when cars-service returns server error")
    void shouldThrowExceptionWhenCarsServiceReturnsServerError() {
        // Arrange
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-3/eligibility"))
                .andExpect(method(GET))
                .andRespond(withServerError());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> adapter.isEligible("vehicle-3"));

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw exception when cars-service is unavailable")
    void shouldThrowExceptionWhenCarsServiceIsUnavailable() {
        // Arrange
        mockServer.expect(requestTo("http://localhost:9991/api/cars/vehicle-4/eligibility"))
                .andExpect(method(GET))
                .andRespond(withServiceUnavailable());

        // Act + Assert
        assertThrows(RuntimeException.class, () -> adapter.isEligible("vehicle-4"));

        mockServer.verify();
    }
}
