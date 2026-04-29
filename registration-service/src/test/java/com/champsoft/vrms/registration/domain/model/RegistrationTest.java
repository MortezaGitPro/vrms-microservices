package com.champsoft.vrms.registration.domain.model;

import com.champsoft.vrms.registration.domain.exception.ExpiryDateMustBeFutureException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class RegistrationTest {

    @Test
    void shouldCreateRegistrationWithActiveStatus() {

        // ------------------- Act -------------------
        // Create a new registration using the factory method createNew().
        // Business rule: a new registration should start as ACTIVE.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(LocalDate.now().plusYears(1))
        );

        // ------------------- Assert -------------------
        // Verify that the registration stores the expected ID.
        assertThat(registration.id().value()).isEqualTo("reg-1");

        // Verify that the registration stores references to vehicle, owner, and agent.
        assertThat(registration.vehicleId().value()).isEqualTo("vehicle-1");
        assertThat(registration.ownerId().value()).isEqualTo("owner-1");
        assertThat(registration.agentId().value()).isEqualTo("agent-1");

        // Verify that the registration stores the expected plate number.
        assertThat(registration.plate().value()).isEqualTo("ABC123");

        // Business rule:
        // A newly created registration should be ACTIVE.
        assertThat(registration.status()).isEqualTo(RegistrationStatus.ACTIVE);
    }

    @Test
    void shouldReturnConvenienceAccessorValues() {

        // ------------------- Arrange -------------------
        // Store the expiry date in a variable so we can compare it later.
        LocalDate expiryDate = LocalDate.now().plusYears(1);

        // Create a valid registration.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(expiryDate)
        );

        // ------------------- Assert -------------------
        // Convenience methods return plain values instead of value objects.
        // These methods are useful for mapping domain objects to DTOs or responses.
        assertThat(registration.vehicleIdValue()).isEqualTo("vehicle-1");
        assertThat(registration.ownerIdValue()).isEqualTo("owner-1");
        assertThat(registration.agentIdValue()).isEqualTo("agent-1");
        assertThat(registration.plateValue()).isEqualTo("ABC123");
        assertThat(registration.expiryValue()).isEqualTo(expiryDate);
    }

    @Test
    void shouldThrowExceptionWhenCreatingRegistrationWithTodayExpiry() {

        // ------------------- Act + Assert -------------------
        // Business rule:
        // A registration expiry date must be in the future.
        // Today is not considered a future date, so it should be rejected.
        assertThrows(ExpiryDateMustBeFutureException.class, () ->
                Registration.createNew(
                        RegistrationId.of("reg-1"),
                        new VehicleRef("vehicle-1"),
                        new OwnerRef("owner-1"),
                        new AgentRef("agent-1"),
                        new PlateNumber("ABC123"),
                        new ExpiryDate(LocalDate.now())
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenCreatingRegistrationWithPastExpiry() {

        // ------------------- Act + Assert -------------------
        // Business rule:
        // A registration expiry date cannot be in the past.
        // Yesterday is invalid and should be rejected.
        assertThrows(ExpiryDateMustBeFutureException.class, () ->
                Registration.createNew(
                        RegistrationId.of("reg-1"),
                        new VehicleRef("vehicle-1"),
                        new OwnerRef("owner-1"),
                        new AgentRef("agent-1"),
                        new PlateNumber("ABC123"),
                        new ExpiryDate(LocalDate.now().minusDays(1))
                )
        );
    }

    @Test
    void shouldRenewRegistrationSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a valid ACTIVE registration.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(LocalDate.now().plusMonths(6))
        );

        // Create a new valid expiry date for renewal.
        ExpiryDate newExpiry = new ExpiryDate(LocalDate.now().plusYears(2));

        // ------------------- Act -------------------
        // Renew the registration with the new future expiry date.
        registration.renew(newExpiry);

        // ------------------- Assert -------------------
        // Verify that the registration expiry date was updated.
        assertThat(registration.expiry()).isEqualTo(newExpiry);
    }

    @Test
    void shouldThrowExceptionWhenRenewingWithTodayExpiry() {

        // ------------------- Arrange -------------------
        // Create a valid ACTIVE registration.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(LocalDate.now().plusMonths(6))
        );

        // Today is invalid because renewal expiry must be in the future.
        ExpiryDate invalidExpiry = new ExpiryDate(LocalDate.now());

        // ------------------- Act + Assert -------------------
        // Business rule:
        // Renewing with today's date should fail.
        assertThrows(ExpiryDateMustBeFutureException.class,
                () -> registration.renew(invalidExpiry));
    }

    @Test
    void shouldThrowExceptionWhenRenewingWithPastExpiry() {

        // ------------------- Arrange -------------------
        // Create a valid ACTIVE registration.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(LocalDate.now().plusMonths(6))
        );

        // A past date is invalid for renewal.
        ExpiryDate invalidExpiry = new ExpiryDate(LocalDate.now().minusDays(1));

        // ------------------- Act + Assert -------------------
        // Business rule:
        // Renewing with a past expiry date should fail.
        assertThrows(ExpiryDateMustBeFutureException.class,
                () -> registration.renew(invalidExpiry));
    }

    @Test
    void shouldCancelRegistrationSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a valid ACTIVE registration.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(LocalDate.now().plusMonths(6))
        );

        // ------------------- Act -------------------
        // Cancel the registration.
        registration.cancel();

        // ------------------- Assert -------------------
        // Business rule:
        // After cancellation, the registration status should be CANCELLED.
        assertThat(registration.status()).isEqualTo(RegistrationStatus.CANCELLED);
    }

    @Test
    void shouldThrowExceptionWhenRenewingCancelledRegistration() {

        // ------------------- Arrange -------------------
        // Create a valid ACTIVE registration.
        Registration registration = Registration.createNew(
                RegistrationId.of("reg-1"),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber("ABC123"),
                new ExpiryDate(LocalDate.now().plusMonths(6))
        );

        // Cancel the registration first.
        registration.cancel();

        // Create a valid future expiry date.
        ExpiryDate newExpiry = new ExpiryDate(LocalDate.now().plusYears(1));

        // ------------------- Act + Assert -------------------
        // Business rule:
        // A CANCELLED registration cannot be renewed.
        assertThrows(RuntimeException.class,
                () -> registration.renew(newExpiry));
    }
}