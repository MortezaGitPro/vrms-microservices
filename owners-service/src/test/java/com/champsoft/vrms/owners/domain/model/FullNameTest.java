package com.champsoft.vrms.owners.domain.model;

import com.champsoft.vrms.owners.domain.exception.InvalidOwnerNameException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class FullNameTest {

    @Test
    void shouldCreateValidFullName() {

        // ------------------- Act -------------------
        // Create a FullName value object with a valid owner name.
        FullName fullName = new FullName("John Smith");

        // ------------------- Assert -------------------
        // Verify that the full name stores the expected value.
        assertThat(fullName.value()).isEqualTo("John Smith");
    }

    @Test
    void shouldTrimFullName() {

        // ------------------- Act -------------------
        // Create a FullName with extra spaces before and after the name.
        // Business rule: the name should be normalized by trimming spaces.
        FullName fullName = new FullName("  John Smith  ");

        // ------------------- Assert -------------------
        // The stored value should not contain the extra spaces.
        assertThat(fullName.value()).isEqualTo("John Smith");
    }

    @Test
    void shouldThrowExceptionWhenFullNameIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: owner full name cannot be null.
        // The constructor should reject null values.
        assertThrows(InvalidOwnerNameException.class, () -> new FullName(null));
    }

    @Test
    void shouldThrowExceptionWhenFullNameIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: owner full name cannot be blank.
        // A string with only spaces is not a valid name.
        assertThrows(InvalidOwnerNameException.class, () -> new FullName("   "));
    }

    @Test
    void shouldThrowExceptionWhenFullNameIsTooShort() {

        // ------------------- Act + Assert -------------------
        // Business rule: owner full name must have a minimum length.
        // A single character is too short and should be rejected.
        assertThrows(InvalidOwnerNameException.class, () -> new FullName("A"));
    }

    @Test
    void shouldThrowExceptionWhenFullNameIsTooLong() {

        // ------------------- Arrange -------------------
        // Create a name that is longer than the allowed maximum length.
        String longName = "A".repeat(121);

        // ------------------- Act + Assert -------------------
        // Business rule: owner full name must not exceed the maximum length.
        // This long name should be rejected by the constructor.
        assertThrows(InvalidOwnerNameException.class, () -> new FullName(longName));
    }

    @Test
    void shouldCompareFullNamesCorrectly() {

        // ------------------- Arrange -------------------
        // Create two FullName objects with the same value.
        FullName name1 = new FullName("John Smith");
        FullName name2 = new FullName("John Smith");

        // Create another FullName object with a different value.
        FullName name3 = new FullName("Jane Smith");

        // ------------------- Assert -------------------
        // Two FullName objects with the same value should be equal.
        assertThat(name1).isEqualTo(name2);

        // FullName objects with different values should not be equal.
        assertThat(name1).isNotEqualTo(name3);

        // Equal objects should have the same hashCode.
        // This is important when objects are used in collections such as HashSet or HashMap.
        assertThat(name1.hashCode()).isEqualTo(name2.hashCode());

        // toString() should return the full name value.
        assertThat(name1.toString()).isEqualTo("John Smith");
    }
}