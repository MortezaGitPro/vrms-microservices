package com.champsoft.vrms.owners.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class OwnerTest {

    @Test
    void shouldCreateOwnerWithInactiveStatus() {

        // ------------------- Act -------------------
        // Create a new Owner domain object.
        // Business rule: a newly created owner should start as INACTIVE.
        Owner owner = new Owner(
                OwnerId.of("owner-1"),
                new FullName("John Smith"),
                new Address("Montreal")
        );

        // ------------------- Assert -------------------
        // Verify that the owner stores the expected ID.
        assertThat(owner.id().value()).isEqualTo("owner-1");

        // Verify that the owner stores the expected full name.
        assertThat(owner.fullName().value()).isEqualTo("John Smith");

        // Verify that the owner stores the expected address.
        assertThat(owner.address().value()).isEqualTo("Montreal");

        // Verify the default business state.
        assertThat(owner.status()).isEqualTo(OwnerStatus.INACTIVE);

        // Business rule:
        // An INACTIVE owner is not eligible for registration.
        assertThat(owner.isEligibleForRegistration()).isFalse();
    }

    @Test
    void shouldActivateOwnerSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a new owner.
        // By default, the owner starts as INACTIVE.
        Owner owner = new Owner(
                OwnerId.of("owner-1"),
                new FullName("John Smith"),
                new Address("Montreal")
        );

        // ------------------- Act -------------------
        // Activate the owner.
        // Business rule: an owner can become ACTIVE.
        owner.activate();

        // ------------------- Assert -------------------
        // After activation, the owner status should be ACTIVE.
        assertThat(owner.status()).isEqualTo(OwnerStatus.ACTIVE);

        // Business rule:
        // Only ACTIVE owners are eligible for registration.
        assertThat(owner.isEligibleForRegistration()).isTrue();
    }

    @Test
    void shouldSuspendOwnerSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a new owner.
        Owner owner = new Owner(
                OwnerId.of("owner-1"),
                new FullName("John Smith"),
                new Address("Montreal")
        );

        // ------------------- Act -------------------
        // Suspend the owner.
        // Business rule: a suspended owner is not allowed to register vehicles.
        owner.suspend();

        // ------------------- Assert -------------------
        // After suspension, the owner status should be SUSPENDED.
        assertThat(owner.status()).isEqualTo(OwnerStatus.SUSPENDED);

        // Business rule:
        // A SUSPENDED owner is not eligible for registration.
        assertThat(owner.isEligibleForRegistration()).isFalse();
    }

    @Test
    void shouldUpdateOwnerSuccessfully() {

        // ------------------- Arrange -------------------
        // Create an owner with initial name and address.
        Owner owner = new Owner(
                OwnerId.of("owner-1"),
                new FullName("John Smith"),
                new Address("Montreal")
        );

        // ------------------- Act -------------------
        // Update the owner's full name and address.
        owner.update(new FullName("Jane Smith"), new Address("Laval"));

        // ------------------- Assert -------------------
        // Verify that the full name was updated.
        assertThat(owner.fullName().value()).isEqualTo("Jane Smith");

        // Verify that the address was updated.
        assertThat(owner.address().value()).isEqualTo("Laval");
    }
}