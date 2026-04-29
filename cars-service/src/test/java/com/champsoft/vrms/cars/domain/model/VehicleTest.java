package com.champsoft.vrms.cars.domain.model;

import com.champsoft.vrms.cars.domain.exception.VehicleAlreadyActiveException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class VehicleTest {

    @Test
    void shouldCreateVehicleWithInactiveStatus() {

        // ------------------- Arrange -------------------
        // Create the required value objects for a Vehicle.
        // These are domain objects, not database entities.
        VehicleId id = VehicleId.of("car-1");
        Vin vin = new Vin("1HGCM82633A123456");
        VehicleSpecs specs = new VehicleSpecs("Toyota", "Corolla", 2020);

        // ------------------- Act -------------------
        // Create a new vehicle using the constructor.
        // Business rule: a newly created vehicle should start as INACTIVE.
        Vehicle vehicle = new Vehicle(id, vin, specs);

        // ------------------- Assert -------------------
        // Verify that the vehicle keeps the same values passed to the constructor.
        assertThat(vehicle.id()).isEqualTo(id);
        assertThat(vehicle.vin()).isEqualTo(vin);
        assertThat(vehicle.specs()).isEqualTo(specs);

        // Verify the default business state.
        // New vehicles are INACTIVE and therefore not eligible for registration yet.
        assertThat(vehicle.status()).isEqualTo(VehicleStatus.INACTIVE);
        assertThat(vehicle.isEligibleForRegistration()).isFalse();
    }

    @Test
    void shouldActivateVehicleSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a new vehicle.
        // By default, this vehicle starts with INACTIVE status.
        Vehicle vehicle = new Vehicle(
                VehicleId.of("car-1"),
                new Vin("1HGCM82633A123456"),
                new VehicleSpecs("Toyota", "Corolla", 2020)
        );

        // ------------------- Act -------------------
        // Activate the vehicle.
        // Business rule: an INACTIVE vehicle can become ACTIVE.
        vehicle.activate();

        // ------------------- Assert -------------------
        // After activation, the vehicle status should be ACTIVE.
        assertThat(vehicle.status()).isEqualTo(VehicleStatus.ACTIVE);

        // Business rule: only ACTIVE vehicles are eligible for registration.
        assertThat(vehicle.isEligibleForRegistration()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenActivatingAlreadyActiveVehicle() {

        // ------------------- Arrange -------------------
        // Create a new vehicle.
        Vehicle vehicle = new Vehicle(
                VehicleId.of("car-1"),
                new Vin("1HGCM82633A123456"),
                new VehicleSpecs("Toyota", "Corolla", 2020)
        );

        // First activation is valid.
        vehicle.activate();

        // ------------------- Act + Assert -------------------
        // Second activation is invalid.
        // Business rule: an already ACTIVE vehicle cannot be activated again.
        assertThrows(VehicleAlreadyActiveException.class, vehicle::activate);
    }

    @Test
    void shouldUpdateVehicleSpecsSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a vehicle with initial specifications.
        Vehicle vehicle = new Vehicle(
                VehicleId.of("car-1"),
                new Vin("1HGCM82633A123456"),
                new VehicleSpecs("Toyota", "Corolla", 2020)
        );

        // New specifications that should replace the old ones.
        VehicleSpecs newSpecs = new VehicleSpecs("Honda", "Civic", 2022);

        // ------------------- Act -------------------
        // Update the vehicle specifications.
        vehicle.updateSpecs(newSpecs);

        // ------------------- Assert -------------------
        // Verify that the whole specs object was replaced.
        assertThat(vehicle.specs()).isEqualTo(newSpecs);

        // Verify each updated field separately to make the test clearer for students.
        assertThat(vehicle.specs().make()).isEqualTo("Honda");
        assertThat(vehicle.specs().model()).isEqualTo("Civic");
        assertThat(vehicle.specs().year()).isEqualTo(2022);
    }
}