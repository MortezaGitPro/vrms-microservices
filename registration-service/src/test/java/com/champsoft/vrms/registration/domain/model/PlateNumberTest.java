package com.champsoft.vrms.registration.domain.model;

import com.champsoft.vrms.registration.domain.exception.InvalidPlateException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class PlateNumberTest {

    @Test
    void shouldCreateValidPlateNumber() {

        // ------------------- Act -------------------
        // Create a PlateNumber value object with a valid plate number.
        PlateNumber plate = new PlateNumber("ABC123");

        // ------------------- Assert -------------------
        // Verify that the plate number stores the expected value.
        assertThat(plate.value()).isEqualTo("ABC123");
    }

    @Test
    void shouldTrimAndUppercasePlateNumber() {

        // ------------------- Act -------------------
        // Create a plate number with extra spaces and lowercase letters.
        // Business rule: plate number should be normalized by trimming spaces
        // and converting letters to uppercase.
        PlateNumber plate = new PlateNumber("  abc123  ");

        // ------------------- Assert -------------------
        // The stored plate number should not contain spaces
        // and should be converted to uppercase.
        assertThat(plate.value()).isEqualTo("ABC123");
    }

    @Test
    void shouldThrowExceptionWhenPlateIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: plate number cannot be null.
        // The constructor should reject null values.
        assertThrows(InvalidPlateException.class, () -> new PlateNumber(null));
    }

    @Test
    void shouldThrowExceptionWhenPlateIsTooShort() {

        // ------------------- Act + Assert -------------------
        // Business rule: plate number must have the required minimum length.
        // A one-character plate number is too short and should be rejected.
        assertThrows(InvalidPlateException.class, () -> new PlateNumber("A"));
    }

    @Test
    void shouldThrowExceptionWhenPlateIsTooLong() {

        // ------------------- Act + Assert -------------------
        // Business rule: plate number must not exceed the maximum allowed length.
        // This value is too long and should be rejected.
        assertThrows(InvalidPlateException.class, () -> new PlateNumber("ABCDEFGHIJK"));
    }

    @Test
    void shouldAcceptMinimumPlateLength() {

        // ------------------- Act -------------------
        // Boundary test:
        // "ABC" is the minimum valid plate length, so it should be accepted.
        PlateNumber plate = new PlateNumber("ABC");

        // ------------------- Assert -------------------
        // Verify that the minimum valid plate number is stored correctly.
        assertThat(plate.value()).isEqualTo("ABC");
    }

    @Test
    void shouldAcceptMaximumPlateLength() {

        // ------------------- Act -------------------
        // Boundary test:
        // "ABCDEFGHIJ" is the maximum valid plate length, so it should be accepted.
        PlateNumber plate = new PlateNumber("ABCDEFGHIJ");

        // ------------------- Assert -------------------
        // Verify that the maximum valid plate number is stored correctly.
        assertThat(plate.value()).isEqualTo("ABCDEFGHIJ");
    }
}