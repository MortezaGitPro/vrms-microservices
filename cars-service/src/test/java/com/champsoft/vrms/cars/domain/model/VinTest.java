package com.champsoft.vrms.cars.domain.model;

import com.champsoft.vrms.cars.domain.exception.InvalidVinException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class VinTest {

    @Test
    void shouldCreateValidVin() {

        // ------------------- Act -------------------
        // Create a VIN value object with a valid VIN.
        Vin vin = new Vin("1HGCM82633A123456");

        // ------------------- Assert -------------------
        // The VIN should store the valid value.
        assertThat(vin.value()).isEqualTo("1HGCM82633A123456");
    }

    @Test
    void shouldTrimAndUppercaseVin() {

        // ------------------- Act -------------------
        // Create a VIN with spaces and lowercase letters.
        // Business rule: VIN should be normalized by trimming spaces
        // and converting letters to uppercase.
        Vin vin = new Vin("  abc12345678  ");

        // ------------------- Assert -------------------
        // The stored VIN should not contain extra spaces
        // and should be converted to uppercase.
        assertThat(vin.value()).isEqualTo("ABC12345678");
    }

    @Test
    void shouldThrowExceptionWhenVinIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: VIN cannot be null.
        // The constructor should reject null values.
        assertThrows(InvalidVinException.class, () -> new Vin(null));
    }

    @Test
    void shouldThrowExceptionWhenVinIsTooShort() {

        // ------------------- Act + Assert -------------------
        // Business rule: VIN must have the required minimum length.
        // A very short VIN should be rejected.
        assertThrows(InvalidVinException.class, () -> new Vin("BADVIN"));
    }

    @Test
    void shouldThrowExceptionWhenVinIsTooLong() {

        // ------------------- Act + Assert -------------------
        // Business rule: VIN must not be longer than the allowed length.
        // This value has too many characters and should be rejected.
        assertThrows(InvalidVinException.class, () -> new Vin("123456789012345678"));
    }

    @Test
    void shouldCompareVinObjectsCorrectly() {

        // ------------------- Arrange -------------------
        // Create two VIN objects with the same value.
        Vin vin1 = new Vin("1HGCM82633A123456");
        Vin vin2 = new Vin("1HGCM82633A123456");

        // Create another VIN object with a different value.
        Vin vin3 = new Vin("2HGCM82633A123456");

        // ------------------- Assert -------------------
        // Two VIN objects with the same value should be equal.
        assertThat(vin1).isEqualTo(vin2);

        // VIN objects with different values should not be equal.
        assertThat(vin1).isNotEqualTo(vin3);

        // Equal objects should have the same hashCode.
        // This is important when objects are used in collections such as HashSet or HashMap.
        assertThat(vin1.hashCode()).isEqualTo(vin2.hashCode());

        // toString() should return the VIN value.
        assertThat(vin1.toString()).isEqualTo("1HGCM82633A123456");
    }
}