package com.champsoft.vrms.owners.domain.model;

import com.champsoft.vrms.owners.domain.exception.InvalidAddressException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class AddressTest {

    @Test
    void shouldCreateValidAddress() {

        // ------------------- Act -------------------
        // Create an Address value object with a valid city/address value.
        Address address = new Address("Montreal");

        // ------------------- Assert -------------------
        // Verify that the address stores the expected value.
        assertThat(address.value()).isEqualTo("Montreal");
    }

    @Test
    void shouldTrimAddress() {

        // ------------------- Act -------------------
        // Create an Address with extra spaces before and after the value.
        // Business rule: address should be normalized by trimming spaces.
        Address address = new Address("  Montreal  ");

        // ------------------- Assert -------------------
        // The stored value should not contain the extra spaces.
        assertThat(address.value()).isEqualTo("Montreal");
    }

    @Test
    void shouldAllowNullAddress() {

        // ------------------- Act -------------------
        // Create an Address with null.
        // Business rule: address is optional, so null is allowed.
        Address address = new Address(null);

        // ------------------- Assert -------------------
        // Since address is optional, the stored value can be null.
        assertThat(address.value()).isNull();
    }

    @Test
    void shouldConvertBlankAddressToNull() {

        // ------------------- Act -------------------
        // Create an Address with only spaces.
        // Business rule: blank address is treated the same as no address.
        Address address = new Address("   ");

        // ------------------- Assert -------------------
        // Blank address should be normalized to null.
        assertThat(address.value()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenAddressIsTooLong() {

        // ------------------- Arrange -------------------
        // Create an address longer than the allowed maximum length.
        String longAddress = "A".repeat(201);

        // ------------------- Act + Assert -------------------
        // Business rule: address must not exceed the maximum length.
        // This long address should be rejected by the constructor.
        assertThrows(InvalidAddressException.class, () -> new Address(longAddress));
    }
}