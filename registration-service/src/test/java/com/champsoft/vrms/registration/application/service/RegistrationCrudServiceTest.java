package com.champsoft.vrms.registration.application.service;

import com.champsoft.vrms.registration.application.exception.RegistrationNotFoundException;
import com.champsoft.vrms.registration.application.port.out.RegistrationRepositoryPort;
import com.champsoft.vrms.registration.domain.exception.ExpiryDateMustBeFutureException;
import com.champsoft.vrms.registration.domain.model.AgentRef;
import com.champsoft.vrms.registration.domain.model.ExpiryDate;
import com.champsoft.vrms.registration.domain.model.OwnerRef;
import com.champsoft.vrms.registration.domain.model.PlateNumber;
import com.champsoft.vrms.registration.domain.model.Registration;
import com.champsoft.vrms.registration.domain.model.RegistrationId;
import com.champsoft.vrms.registration.domain.model.RegistrationStatus;
import com.champsoft.vrms.registration.domain.model.VehicleRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Enables Mockito for this test class.
// This test does not start Spring and does not use a real database.
@ExtendWith(MockitoExtension.class)
class RegistrationCrudServiceTest {

    // Mocked repository port.
    // The service will use this fake repository instead of a real database.
    @Mock
    private RegistrationRepositoryPort repo;

    // Real service under test.
    // Mockito injects the mocked repository into this service.
    @InjectMocks
    private RegistrationCrudService service;

    // Helper method to create a valid Registration object.
    // This avoids repeating the same object creation in every test.
    private Registration sampleRegistration(String id, String plate) {
        return Registration.createNew(
                RegistrationId.of(id),
                new VehicleRef("vehicle-1"),
                new OwnerRef("owner-1"),
                new AgentRef("agent-1"),
                new PlateNumber(plate),
                new ExpiryDate(LocalDate.now().plusYears(1))
        );
    }

    // Tests related to reading registrations.
    @Nested
    @DisplayName("Read registration")
    class ReadRegistrationTests {

        @Test
        void shouldReturnRegistrationWhenGettingById() {
            // Arrange: repository returns an existing registration.
            Registration registration = sampleRegistration("reg-1", "ABC123");
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.of(registration));

            // Act: call the service method.
            Registration found = service.get("reg-1");

            // Assert: service returns the registration from the repository.
            assertThat(found).isSameAs(registration);

            // Verify repository was used to search by id.
            verify(repo).findById(any(RegistrationId.class));
        }

        @Test
        void shouldThrowRegistrationNotFoundExceptionWhenGettingMissingRegistration() {
            // Arrange: repository cannot find the registration.
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.empty());

            // Act + Assert: service should throw a not-found exception.
            assertThrows(RegistrationNotFoundException.class, () -> service.get("missing-reg"));

            // Verify repository lookup happened.
            verify(repo).findById(any(RegistrationId.class));
        }

        @Test
        void shouldReturnAllRegistrations() {
            // Arrange: repository returns a list of registrations.
            List<Registration> registrations = List.of(
                    sampleRegistration("reg-1", "ABC123"),
                    sampleRegistration("reg-2", "XYZ789")
            );
            when(repo.findAll()).thenReturn(registrations);

            // Act: call the list method.
            List<Registration> result = service.list();

            // Assert: service returns all registrations from repository.
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(registrations);

            // Verify repository findAll was called.
            verify(repo).findAll();
        }
    }

    // Tests related to renewing registrations.
    @Nested
    @DisplayName("Renew registration")
    class RenewRegistrationTests {

        @Test
        void shouldRenewRegistrationSuccessfully() {
            // Arrange: existing registration and a valid future expiry date.
            Registration registration = sampleRegistration("reg-1", "ABC123");
            LocalDate newExpiry = LocalDate.now().plusYears(2);

            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.of(registration));

            // Simulate saving and returning the updated registration.
            when(repo.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: renew the registration.
            Registration renewed = service.renew("reg-1", newExpiry);

            // Assert: expiry date is updated and status remains ACTIVE.
            assertThat(renewed.expiry().value()).isEqualTo(newExpiry);
            assertThat(renewed.status()).isEqualTo(RegistrationStatus.ACTIVE);

            // Verify service searched first, then saved the updated registration.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo).save(registration);
        }

        @Test
        void shouldThrowRegistrationNotFoundExceptionWhenRenewingMissingRegistration() {
            // Arrange: registration does not exist.
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.empty());

            // Act + Assert: cannot renew a missing registration.
            assertThrows(RegistrationNotFoundException.class,
                    () -> service.renew("missing-reg", LocalDate.now().plusYears(2)));

            // Verify lookup happened, but save did not happen.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowExpiryDateMustBeFutureExceptionWhenRenewingWithPastExpiry() {
            // Arrange: registration exists, but new expiry date is in the past.
            Registration registration = sampleRegistration("reg-1", "ABC123");
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.of(registration));

            // Act + Assert: domain rule rejects past expiry date.
            assertThrows(ExpiryDateMustBeFutureException.class,
                    () -> service.renew("reg-1", LocalDate.now().minusDays(1)));

            // Verify lookup happened, but invalid renewal was not saved.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowRuntimeExceptionWhenRenewingCancelledRegistration() {
            // Arrange: registration exists but is already cancelled.
            Registration registration = sampleRegistration("reg-1", "ABC123");
            registration.cancel();
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.of(registration));

            // Act + Assert: cancelled registration cannot be renewed.
            assertThrows(RuntimeException.class,
                    () -> service.renew("reg-1", LocalDate.now().plusYears(2)));

            // Verify lookup happened, but save did not happen.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo, never()).save(any(Registration.class));
        }
    }

    // Tests related to cancelling registrations.
    @Nested
    @DisplayName("Cancel registration")
    class CancelRegistrationTests {

        @Test
        void shouldCancelRegistrationSuccessfully() {
            // Arrange: registration exists and can be cancelled.
            Registration registration = sampleRegistration("reg-1", "ABC123");
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.of(registration));

            // Simulate saving and returning the cancelled registration.
            when(repo.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: cancel the registration.
            Registration cancelled = service.cancel("reg-1");

            // Assert: status becomes CANCELLED.
            assertThat(cancelled.status()).isEqualTo(RegistrationStatus.CANCELLED);

            // Verify service searched first, then saved the cancelled registration.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo).save(registration);
        }

        @Test
        void shouldThrowRegistrationNotFoundExceptionWhenCancellingMissingRegistration() {
            // Arrange: registration does not exist.
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.empty());

            // Act + Assert: cannot cancel a missing registration.
            assertThrows(RegistrationNotFoundException.class, () -> service.cancel("missing-reg"));

            // Verify lookup happened, but save did not happen.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo, never()).save(any(Registration.class));
        }
    }

    // Tests related to deleting registrations.
    @Nested
    @DisplayName("Delete registration")
    class DeleteRegistrationTests {

        @Test
        void shouldDeleteRegistrationSuccessfully() {
            // Arrange: registration exists.
            Registration registration = sampleRegistration("reg-1", "ABC123");
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.of(registration));

            // Act: delete the registration.
            service.delete("reg-1");

            // Assert: service searched first, then deleted by id.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo).deleteById(any(RegistrationId.class));
        }

        @Test
        void shouldThrowRegistrationNotFoundExceptionWhenDeletingMissingRegistration() {
            // Arrange: registration does not exist.
            when(repo.findById(any(RegistrationId.class))).thenReturn(Optional.empty());

            // Act + Assert: cannot delete a missing registration.
            assertThrows(RegistrationNotFoundException.class, () -> service.delete("missing-reg"));

            // Verify lookup happened, but delete did not happen.
            verify(repo).findById(any(RegistrationId.class));
            verify(repo, never()).deleteById(any(RegistrationId.class));
        }
    }
}