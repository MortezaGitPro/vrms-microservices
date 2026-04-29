package com.champsoft.vrms.registration.application.service;

import com.champsoft.vrms.registration.application.exception.CrossContextValidationException;
import com.champsoft.vrms.registration.application.exception.PlateAlreadyTakenException;
import com.champsoft.vrms.registration.application.port.out.AgentEligibilityPort;
import com.champsoft.vrms.registration.application.port.out.OwnerEligibilityPort;
import com.champsoft.vrms.registration.application.port.out.RegistrationRepositoryPort;
import com.champsoft.vrms.registration.application.port.out.VehicleEligibilityPort;
import com.champsoft.vrms.registration.domain.exception.ExpiryDateMustBeFutureException;
import com.champsoft.vrms.registration.domain.exception.InvalidPlateException;
import com.champsoft.vrms.registration.domain.model.Registration;
import com.champsoft.vrms.registration.domain.model.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Enables Mockito for this test class.
// This means we can use @Mock and @InjectMocks without starting Spring.
@ExtendWith(MockitoExtension.class)
class RegistrationOrchestratorTest {

    // Mocked port for checking if the vehicle is eligible.
    // No real cars-service HTTP call is made.
    @Mock
    private VehicleEligibilityPort vehiclePort;

    // Mocked port for checking if the owner is eligible.
    // No real owners-service HTTP call is made.
    @Mock
    private OwnerEligibilityPort ownerPort;

    // Mocked port for checking if the agent is eligible.
    // No real agents-service HTTP call is made.
    @Mock
    private AgentEligibilityPort agentPort;

    // Mocked repository port.
    // No real database is used in this service-layer test.
    @Mock
    private RegistrationRepositoryPort repo;

    // This is the real service we are testing.
    // Mockito injects the mocked ports and repository into it.
    @InjectMocks
    private RegistrationOrchestrator orchestrator;

    // Groups all successful registration orchestration tests.
    @Nested
    @DisplayName("Successful registration orchestration")
    class SuccessfulRegistrationTests {

        @Test
        void shouldRegisterSuccessfullyWhenAllChecksPass() {
            // Arrange: create a valid future expiry date.
            LocalDate expiry = LocalDate.now().plusYears(1);

            // Plate is not already taken, so registration can continue.
            when(repo.existsByPlate("ABC123")).thenReturn(false);

            // All downstream eligibility checks pass.
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(true);
            when(ownerPort.isEligible("owner-1")).thenReturn(true);
            when(agentPort.isEligible("agent-1")).thenReturn(true);

            // When the orchestrator saves the registration,
            // return the same object passed to repo.save(...).
            when(repo.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: call the real orchestration method.
            Registration saved = orchestrator.register(
                    "vehicle-1",
                    "owner-1",
                    "agent-1",
                    "ABC123",
                    expiry
            );

            // Assert: verify the registration object was created correctly.
            assertThat(saved).isNotNull();
            assertThat(saved.id()).isNotNull();
            assertThat(saved.vehicleId().value()).isEqualTo("vehicle-1");
            assertThat(saved.ownerId().value()).isEqualTo("owner-1");
            assertThat(saved.agentId().value()).isEqualTo("agent-1");
            assertThat(saved.plate().value()).isEqualTo("ABC123");
            assertThat(saved.expiry().value()).isEqualTo(expiry);
            assertThat(saved.status()).isEqualTo(RegistrationStatus.ACTIVE);

            // Verify the expected orchestration steps were called.
            verify(repo).existsByPlate("ABC123");
            verify(vehiclePort).isEligible("vehicle-1");
            verify(ownerPort).isEligible("owner-1");
            verify(agentPort).isEligible("agent-1");
            verify(repo).save(any(Registration.class));
        }

        @Test
        void shouldSaveRegistrationWithExpectedValues() {
            // Arrange: prepare a valid registration request.
            LocalDate expiry = LocalDate.now().plusYears(1);

            // Plate is available.
            when(repo.existsByPlate("XYZ789")).thenReturn(false);

            // All downstream services approve the registration.
            when(vehiclePort.isEligible("vehicle-99")).thenReturn(true);
            when(ownerPort.isEligible("owner-99")).thenReturn(true);
            when(agentPort.isEligible("agent-99")).thenReturn(true);

            // Simulate repository save.
            when(repo.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act: register using different values so we can verify what was saved.
            orchestrator.register("vehicle-99", "owner-99", "agent-99", "XYZ789", expiry);

            // Assert: capture the exact Registration object sent to repo.save(...).
            ArgumentCaptor<Registration> captor = ArgumentCaptor.forClass(Registration.class);
            verify(repo).save(captor.capture());

            Registration savedRegistration = captor.getValue();

            // Verify the orchestrator built the registration with the expected values.
            assertThat(savedRegistration.vehicleId().value()).isEqualTo("vehicle-99");
            assertThat(savedRegistration.ownerId().value()).isEqualTo("owner-99");
            assertThat(savedRegistration.agentId().value()).isEqualTo("agent-99");
            assertThat(savedRegistration.plate().value()).isEqualTo("XYZ789");
            assertThat(savedRegistration.expiry().value()).isEqualTo(expiry);
            assertThat(savedRegistration.status()).isEqualTo(RegistrationStatus.ACTIVE);
        }
    }

    // Groups all failure scenarios for registration orchestration.
    @Nested
    @DisplayName("Registration orchestration failures")
    class RegistrationFailureTests {

        @Test
        void shouldThrowPlateAlreadyTakenExceptionWhenPlateAlreadyExists() {
            // Arrange: the plate already exists in the system.
            when(repo.existsByPlate("ABC123")).thenReturn(true);

            // Act + Assert: registration should fail immediately.
            assertThrows(PlateAlreadyTakenException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // Since the plate is already taken, no downstream checks should happen.
            verify(repo).existsByPlate("ABC123");
            verify(vehiclePort, never()).isEligible(anyString());
            verify(ownerPort, never()).isEligible(anyString());
            verify(agentPort, never()).isEligible(anyString());

            // Registration must not be saved when validation fails.
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowCrossContextValidationExceptionWhenVehicleIsNotEligible() {
            // Arrange: plate is available, but vehicle is not eligible.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(false);

            // Act + Assert: orchestration should fail because vehicle validation failed.
            assertThrows(CrossContextValidationException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // Vehicle eligibility was checked.
            verify(vehiclePort).isEligible("vehicle-1");

            // Because vehicle failed, owner and agent checks should not happen.
            verify(ownerPort, never()).isEligible(anyString());
            verify(agentPort, never()).isEligible(anyString());

            // Registration must not be saved.
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowCrossContextValidationExceptionWhenOwnerIsNotEligible() {
            // Arrange: plate and vehicle are valid, but owner is not eligible.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(true);
            when(ownerPort.isEligible("owner-1")).thenReturn(false);

            // Act + Assert: orchestration should fail because owner validation failed.
            assertThrows(CrossContextValidationException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // Vehicle and owner checks should happen.
            verify(vehiclePort).isEligible("vehicle-1");
            verify(ownerPort).isEligible("owner-1");

            // Agent check should not happen because owner validation already failed.
            verify(agentPort, never()).isEligible(anyString());

            // Registration must not be saved.
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowCrossContextValidationExceptionWhenAgentIsNotEligible() {
            // Arrange: plate, vehicle, and owner are valid, but agent is not eligible.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(true);
            when(ownerPort.isEligible("owner-1")).thenReturn(true);
            when(agentPort.isEligible("agent-1")).thenReturn(false);

            // Act + Assert: orchestration should fail because agent validation failed.
            assertThrows(CrossContextValidationException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // All eligibility checks up to the failing agent check should happen.
            verify(vehiclePort).isEligible("vehicle-1");
            verify(ownerPort).isEligible("owner-1");
            verify(agentPort).isEligible("agent-1");

            // Registration must not be saved.
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldPropagateRuntimeExceptionWhenVehicleEligibilityCallFails() {
            // Arrange: plate is available, but cars-service call fails.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1"))
                    .thenThrow(new RuntimeException("cars-service unavailable"));

            // Act + Assert: the failure from the vehicle dependency is propagated.
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // Verify the exception message.
            assertThat(ex.getMessage()).isEqualTo("cars-service unavailable");

            // Vehicle check was attempted, but save must not happen.
            verify(vehiclePort).isEligible("vehicle-1");
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldPropagateRuntimeExceptionWhenOwnerEligibilityCallFails() {
            // Arrange: vehicle is eligible, but owners-service call fails.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(true);
            when(ownerPort.isEligible("owner-1"))
                    .thenThrow(new RuntimeException("owners-service unavailable"));

            // Act + Assert: the failure from the owner dependency is propagated.
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // Verify the exception message.
            assertThat(ex.getMessage()).isEqualTo("owners-service unavailable");

            // Vehicle and owner checks were attempted, but save must not happen.
            verify(vehiclePort).isEligible("vehicle-1");
            verify(ownerPort).isEligible("owner-1");
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldPropagateRuntimeExceptionWhenAgentEligibilityCallFails() {
            // Arrange: vehicle and owner are eligible, but agents-service call fails.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(true);
            when(ownerPort.isEligible("owner-1")).thenReturn(true);
            when(agentPort.isEligible("agent-1"))
                    .thenThrow(new RuntimeException("agents-service unavailable"));

            // Act + Assert: the failure from the agent dependency is propagated.
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().plusYears(1)
                    ));

            // Verify the exception message.
            assertThat(ex.getMessage()).isEqualTo("agents-service unavailable");

            // All checks up to the failing agent call were attempted.
            verify(vehiclePort).isEligible("vehicle-1");
            verify(ownerPort).isEligible("owner-1");
            verify(agentPort).isEligible("agent-1");

            // Registration must not be saved.
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowInvalidPlateExceptionWhenPlateIsInvalid() {
            // Act + Assert: invalid plate fails before repository or external calls.
            assertThrows(InvalidPlateException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "A",
                            LocalDate.now().plusYears(1)
                    ));

            // No repository check should happen because the plate value itself is invalid.
            verify(repo, never()).existsByPlate(anyString());

            // No external services should be called.
            verify(vehiclePort, never()).isEligible(anyString());
            verify(ownerPort, never()).isEligible(anyString());
            verify(agentPort, never()).isEligible(anyString());

            // Registration must not be saved.
            verify(repo, never()).save(any(Registration.class));
        }

        @Test
        void shouldThrowExpiryDateMustBeFutureExceptionWhenExpiryIsInPast() {
            // Arrange: all cross-context checks pass.
            when(repo.existsByPlate("ABC123")).thenReturn(false);
            when(vehiclePort.isEligible("vehicle-1")).thenReturn(true);
            when(ownerPort.isEligible("owner-1")).thenReturn(true);
            when(agentPort.isEligible("agent-1")).thenReturn(true);

            // Act + Assert: registration creation fails because expiry date is in the past.
            assertThrows(ExpiryDateMustBeFutureException.class,
                    () -> orchestrator.register(
                            "vehicle-1",
                            "owner-1",
                            "agent-1",
                            "ABC123",
                            LocalDate.now().minusDays(1)
                    ));

            // These validations happen before the domain object rejects the past expiry date.
            verify(repo).existsByPlate("ABC123");
            verify(vehiclePort).isEligible("vehicle-1");
            verify(ownerPort).isEligible("owner-1");
            verify(agentPort).isEligible("agent-1");

            // Even though checks passed, invalid expiry prevents saving.
            verify(repo, never()).save(any(Registration.class));
        }
    }
}