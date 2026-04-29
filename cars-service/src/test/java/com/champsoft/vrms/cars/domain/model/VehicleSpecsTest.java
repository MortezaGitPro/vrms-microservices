package com.champsoft.vrms.cars.domain.model;

import com.champsoft.vrms.cars.domain.exception.InvalidVehicleYearException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class VehicleSpecsTest {

    @Test
    void shouldCreateValidVehicleSpecs() {

        // ------------------- Act -------------------
        // Create a VehicleSpecs value object with valid data.
        VehicleSpecs specs = new VehicleSpecs("Toyota", "Corolla", 2020);

        // ------------------- Assert -------------------
        // Verify that the object stores the same make, model, and year.
        assertThat(specs.make()).isEqualTo("Toyota");
        assertThat(specs.model()).isEqualTo("Corolla");
        assertThat(specs.year()).isEqualTo(2020);
    }

    @Test
    void shouldThrowExceptionWhenMakeIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle make cannot be null.
        // Example: "Toyota" is valid, but null is not valid.
        assertThrows(IllegalArgumentException.class,
                () -> new VehicleSpecs(null, "Corolla", 2020));
    }

    @Test
    void shouldThrowExceptionWhenMakeIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle make cannot be blank.
        // A string with only spaces is not a valid make.
        assertThrows(IllegalArgumentException.class,
                () -> new VehicleSpecs("   ", "Corolla", 2020));
    }

    @Test
    void shouldThrowExceptionWhenModelIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle model cannot be null.
        // Example: "Corolla" is valid, but null is not valid.
        assertThrows(IllegalArgumentException.class,
                () -> new VehicleSpecs("Toyota", null, 2020));
    }

    @Test
    void shouldThrowExceptionWhenModelIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle model cannot be blank.
        // A string with only spaces is not a valid model.
        assertThrows(IllegalArgumentException.class,
                () -> new VehicleSpecs("Toyota", "   ", 2020));
    }

    @Test
    void shouldThrowExceptionWhenYearIsTooOld() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle year must be at least 1980.
        // 1979 is below the allowed minimum, so it should be rejected.
        assertThrows(InvalidVehicleYearException.class,
                () -> new VehicleSpecs("Toyota", "Corolla", 1979));
    }

    @Test
    void shouldThrowExceptionWhenYearIsTooFarInFuture() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle year must not be greater than 2050.
        // 2051 is above the allowed maximum, so it should be rejected.
        assertThrows(InvalidVehicleYearException.class,
                () -> new VehicleSpecs("Toyota", "Corolla", 2051));
    }

    @Test
    void shouldAcceptMinimumValidYear() {

        // ------------------- Act -------------------
        // Boundary test:
        // 1980 is the minimum valid year, so it should be accepted.
        VehicleSpecs specs = new VehicleSpecs("Toyota", "Corolla", 1980);

        // ------------------- Assert -------------------
        // Verify that the minimum valid year is stored correctly.
        assertThat(specs.year()).isEqualTo(1980);
    }

    @Test
    void shouldAcceptMaximumValidYear() {

        // ------------------- Act -------------------
        // Boundary test:
        // 2050 is the maximum valid year, so it should be accepted.
        VehicleSpecs specs = new VehicleSpecs("Toyota", "Corolla", 2050);

        // ------------------- Assert -------------------
        // Verify that the maximum valid year is stored correctly.
        assertThat(specs.year()).isEqualTo(2050);
    }
}