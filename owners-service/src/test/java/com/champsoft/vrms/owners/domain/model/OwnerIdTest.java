package com.champsoft.vrms.owners.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class OwnerIdTest {

    @Test
    void shouldCreateOwnerIdFromValue() {

        // ------------------- Act -------------------
        // Create an OwnerId value object from a fixed string value.
        // This is useful when we already know the ID, for example in tests or when loading existing data.
        OwnerId id = OwnerId.of("owner-1");

        // ------------------- Assert -------------------
        // Verify that the OwnerId stores the expected value.
        assertThat(id.value()).isEqualTo("owner-1");

        // toString() should also return the ID value.
        // This makes the object easier to print, log, or compare in simple messages.
        assertThat(id.toString()).isEqualTo("owner-1");
    }

    @Test
    void shouldCreateNewOwnerId() {

        // ------------------- Act -------------------
        // Create a new OwnerId automatically.
        // Usually this method generates a unique ID for a new owner.
        OwnerId id = OwnerId.newId();

        // ------------------- Assert -------------------
        // The generated ID object should not be null.
        assertThat(id).isNotNull();

        // The generated ID value should not be empty or blank.
        assertThat(id.value()).isNotBlank();
    }

    @Test
    void shouldCompareOwnerIdsCorrectly() {

        // ------------------- Arrange -------------------
        // Create two OwnerId objects with the same value.
        OwnerId id1 = OwnerId.of("owner-1");
        OwnerId id2 = OwnerId.of("owner-1");

        // Create another OwnerId object with a different value.
        OwnerId id3 = OwnerId.of("owner-2");

        // ------------------- Assert -------------------
        // Two OwnerId objects with the same value should be equal.
        assertThat(id1).isEqualTo(id2);

        // OwnerId objects with different values should not be equal.
        assertThat(id1).isNotEqualTo(id3);

        // Equal objects should have the same hashCode.
        // This is important when objects are used in collections such as HashSet or HashMap.
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}