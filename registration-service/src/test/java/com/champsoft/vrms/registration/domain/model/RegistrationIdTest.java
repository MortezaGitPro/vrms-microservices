package com.champsoft.vrms.registration.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class RegistrationIdTest {

    @Test
    void shouldCreateRegistrationIdFromValue() {

        // ------------------- Act -------------------
        // Create a RegistrationId value object from a fixed string value.
        // This is useful when we already know the ID, for example in tests or when loading existing data.
        RegistrationId id = RegistrationId.of("reg-1");

        // ------------------- Assert -------------------
        // Verify that the RegistrationId stores the expected value.
        assertThat(id.value()).isEqualTo("reg-1");
    }

    @Test
    void shouldCreateNewRegistrationId() {

        // ------------------- Act -------------------
        // Create a new RegistrationId automatically.
        // Usually this method generates a unique ID for a new registration.
        RegistrationId id = RegistrationId.newId();

        // ------------------- Assert -------------------
        // The generated ID object should not be null.
        assertThat(id).isNotNull();

        // The generated ID value should not be empty or blank.
        assertThat(id.value()).isNotBlank();
    }
}