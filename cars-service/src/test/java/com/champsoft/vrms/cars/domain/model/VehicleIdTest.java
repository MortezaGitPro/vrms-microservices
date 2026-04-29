package com.champsoft.vrms.cars.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class VehicleIdTest {

    @Test
    void shouldCreateVehicleIdFromValue() {

        // ------------------- Act -------------------
        // Create a VehicleId value object from a fixed string value.
        // This is useful when we already know the ID, for example in tests or when loading existing data.
        VehicleId id = VehicleId.of("car-1");

        // ------------------- Assert -------------------
        // Verify that the VehicleId stores the expected value.
        assertThat(id.value()).isEqualTo("car-1");

        // toString() should also return the ID value.
        // This makes the object easier to print, log, or compare in simple messages.
        assertThat(id.toString()).isEqualTo("car-1");
    }

    @Test
    void shouldCreateNewVehicleId() {

        // ------------------- Act -------------------
        // Create a new VehicleId automatically.
        // Usually this method generates a unique ID for a new vehicle.
        VehicleId id = VehicleId.newId();

        // ------------------- Assert -------------------
        // The generated ID object should not be null.
        assertThat(id).isNotNull();

        // The generated ID value should not be empty or blank.
        assertThat(id.value()).isNotBlank();
    }

    @Test
    void shouldCompareVehicleIdsCorrectly() {

        // ------------------- Arrange -------------------
        // Create two VehicleId objects with the same value.
        VehicleId id1 = VehicleId.of("car-1");
        VehicleId id2 = VehicleId.of("car-1");

        // Create another VehicleId object with a different value.
        VehicleId id3 = VehicleId.of("car-2");

        // ------------------- Assert -------------------
        // Two VehicleId objects with the same value should be equal.
        assertThat(id1).isEqualTo(id2);

        // VehicleId objects with different values should not be equal.
        assertThat(id1).isNotEqualTo(id3);

        // Equal objects should have the same hashCode.
        // This is important when objects are used in collections such as HashSet or HashMap.
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}