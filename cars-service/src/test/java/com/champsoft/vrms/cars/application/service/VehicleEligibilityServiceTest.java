package com.champsoft.vrms.cars.application.service;

import com.champsoft.vrms.cars.application.exception.VehicleNotFoundException;
import com.champsoft.vrms.cars.application.port.out.VehicleRepositoryPort;
import com.champsoft.vrms.cars.domain.model.Vehicle;
import com.champsoft.vrms.cars.domain.model.VehicleId;
import com.champsoft.vrms.cars.domain.model.VehicleSpecs;
import com.champsoft.vrms.cars.domain.model.Vin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// Service-layer unit test → tests application logic only
// NO Spring Boot context, NO real database
// Mockito is used to fake the repository dependency
@ExtendWith(MockitoExtension.class)
class VehicleEligibilityServiceTest {

    // Mock repository:
    // This fake object replaces the real repository/database.
    @Mock
    private VehicleRepositoryPort repo;

    // InjectMocks:
    // Mockito creates VehicleEligibilityService and injects the mocked repo into it.
    @InjectMocks
    private VehicleEligibilityService service;

    @Test
    void shouldReturnTrueWhenVehicleIsActive() {

        // ------------------- Arrange -------------------
        // Create a sample vehicle.
        Vehicle vehicle = sampleVehicle();

        // Activate the vehicle.
        // Business rule: only ACTIVE vehicles are eligible for registration.
        vehicle.activate();

        // Tell the mock repository to return this vehicle when "car-1" is searched.
        when(repo.findById(VehicleId.of("car-1"))).thenReturn(Optional.of(vehicle));

        // ------------------- Act -------------------
        // Ask the service if the vehicle is eligible for registration.
        boolean result = service.isEligible("car-1");

        // ------------------- Assert -------------------
        // Since the vehicle is ACTIVE, eligibility should be true.
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenVehicleIsInactive() {

        // ------------------- Arrange -------------------
        // Create a sample vehicle.
        // By default, a new vehicle starts as INACTIVE.
        Vehicle vehicle = sampleVehicle();

        // Tell the mock repository to return this inactive vehicle.
        when(repo.findById(VehicleId.of("car-1"))).thenReturn(Optional.of(vehicle));

        // ------------------- Act -------------------
        // Ask the service if the vehicle is eligible for registration.
        boolean result = service.isEligible("car-1");

        // ------------------- Assert -------------------
        // Since the vehicle is INACTIVE, eligibility should be false.
        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {

        // ------------------- Arrange -------------------
        // Tell the mock repository that no vehicle exists with this ID.
        when(repo.findById(VehicleId.of("missing"))).thenReturn(Optional.empty());

        // ------------------- Act + Assert -------------------
        // Business rule:
        // If the vehicle does not exist, eligibility cannot be checked.
        // The service should throw VehicleNotFoundException.
        assertThrows(VehicleNotFoundException.class,
                () -> service.isEligible("missing"));
    }

    // Helper method:
    // Creates a reusable sample vehicle for the tests above.
    // This avoids repeating the same object creation code in every test.
    private Vehicle sampleVehicle() {
        return new Vehicle(
                VehicleId.of("car-1"),
                new Vin("1HGCM82633A123456"),
                new VehicleSpecs("Toyota", "Corolla", 2020)
        );
    }
}