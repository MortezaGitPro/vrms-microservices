package com.champsoft.vrms.owners.application.service;

import com.champsoft.vrms.owners.application.exception.DuplicateOwnerException;
import com.champsoft.vrms.owners.application.exception.OwnerNotFoundException;
import com.champsoft.vrms.owners.application.port.out.OwnerRepositoryPort;
import com.champsoft.vrms.owners.domain.model.Address;
import com.champsoft.vrms.owners.domain.model.FullName;
import com.champsoft.vrms.owners.domain.model.Owner;
import com.champsoft.vrms.owners.domain.model.OwnerId;
import com.champsoft.vrms.owners.domain.model.OwnerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// Enable Mockito → this is a pure service-layer test
// No Spring Boot, no database, no HTTP calls
@ExtendWith(MockitoExtension.class)
class OwnerCrudServiceTest {

    // Mocked repository → replaces real database
    // We fully control its behavior in each test
    @Mock
    private OwnerRepositoryPort repo;

    // Real service under test
    // Mockito injects the mocked repository into this service
    @InjectMocks
    private OwnerCrudService service;

    // Helper method → creates valid Owner objects
    // Avoids repeating object creation in every test
    private Owner sampleOwner(String id, String fullName, String address) {
        return new Owner(
                OwnerId.of(id),
                new FullName(fullName),
                new Address(address)
        );
    }

    @Nested
    @DisplayName("Create owner")
    class CreateOwnerTests {

        @Test
        void shouldCreateOwnerSuccessfully() {

            // ------------------- Arrange -------------------
            // Owner name does NOT exist → creation allowed
            when(repo.existsByFullName("John Smith")).thenReturn(false);

            // Simulate repository save → return same object
            when(repo.save(any(Owner.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Owner saved = service.create("John Smith", "Montreal");

            // ------------------- Assert -------------------
            assertThat(saved).isNotNull();
            assertThat(saved.id()).isNotNull();

            // Verify business data
            assertThat(saved.fullName().value()).isEqualTo("John Smith");
            assertThat(saved.address().value()).isEqualTo("Montreal");

            // Domain rule → new owner starts INACTIVE
            assertThat(saved.status()).isEqualTo(OwnerStatus.INACTIVE);

            // Verify correct flow → check duplicate then save
            verify(repo).existsByFullName("John Smith");
            verify(repo).save(any(Owner.class));
        }

        @Test
        void shouldSaveOwnerWithExpectedValues() {

            // ------------------- Arrange -------------------
            when(repo.existsByFullName("Jane Smith")).thenReturn(false);
            when(repo.save(any(Owner.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            service.create("Jane Smith", "Laval");

            // ------------------- Assert -------------------
            // Capture object passed to repository
            ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
            verify(repo).save(captor.capture());

            Owner ownerPassedToRepository = captor.getValue();

            // Verify service built correct object BEFORE saving
            assertThat(ownerPassedToRepository.fullName().value()).isEqualTo("Jane Smith");
            assertThat(ownerPassedToRepository.address().value()).isEqualTo("Laval");
            assertThat(ownerPassedToRepository.status()).isEqualTo(OwnerStatus.INACTIVE);
        }

        @Test
        void shouldThrowDuplicateOwnerExceptionWhenOwnerNameAlreadyExists() {

            // ------------------- Arrange -------------------
            // Duplicate name → should fail
            when(repo.existsByFullName("John Smith")).thenReturn(true);

            // ------------------- Act + Assert -------------------
            assertThrows(DuplicateOwnerException.class,
                    () -> service.create("John Smith", "Montreal"));

            // Verify no save happens
            verify(repo).existsByFullName("John Smith");
            verify(repo, never()).save(any(Owner.class));
        }
    }

    @Nested
    @DisplayName("Read owner")
    class ReadOwnerTests {

        @Test
        void shouldReturnOwnerWhenGettingById() {

            // ------------------- Arrange -------------------
            Owner owner = sampleOwner("owner-1", "John Smith", "Montreal");
            when(repo.findById(OwnerId.of("owner-1")))
                    .thenReturn(Optional.of(owner));

            // ------------------- Act -------------------
            Owner found = service.getById("owner-1");

            // ------------------- Assert -------------------
            assertThat(found).isSameAs(owner);

            // Verify repository call
            verify(repo).findById(OwnerId.of("owner-1"));
        }

        @Test
        void shouldThrowOwnerNotFoundExceptionWhenGettingMissingOwner() {

            // ------------------- Arrange -------------------
            when(repo.findById(OwnerId.of("missing-owner")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(OwnerNotFoundException.class,
                    () -> service.getById("missing-owner"));

            verify(repo).findById(OwnerId.of("missing-owner"));
        }

        @Test
        void shouldReturnAllOwners() {

            // ------------------- Arrange -------------------
            List<Owner> owners = List.of(
                    sampleOwner("owner-1", "John Smith", "Montreal"),
                    sampleOwner("owner-2", "Jane Smith", "Laval")
            );
            when(repo.findAll()).thenReturn(owners);

            // ------------------- Act -------------------
            List<Owner> result = service.list();

            // ------------------- Assert -------------------
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(owners);

            verify(repo).findAll();
        }
    }

    @Nested
    @DisplayName("Update owner")
    class UpdateOwnerTests {

        @Test
        void shouldUpdateOwnerSuccessfully() {

            // ------------------- Arrange -------------------
            Owner owner = sampleOwner("owner-1", "John Smith", "Montreal");

            when(repo.findById(OwnerId.of("owner-1")))
                    .thenReturn(Optional.of(owner));

            when(repo.save(any(Owner.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Owner updated = service.update("owner-1", "Jane Smith", "Laval");

            // ------------------- Assert -------------------
            assertThat(updated.fullName().value()).isEqualTo("Jane Smith");
            assertThat(updated.address().value()).isEqualTo("Laval");

            verify(repo).findById(OwnerId.of("owner-1"));
            verify(repo).save(owner);
        }

        @Test
        void shouldThrowOwnerNotFoundExceptionWhenUpdatingMissingOwner() {

            // ------------------- Arrange -------------------
            when(repo.findById(OwnerId.of("missing-owner")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(OwnerNotFoundException.class,
                    () -> service.update("missing-owner", "Jane Smith", "Laval"));

            verify(repo).findById(OwnerId.of("missing-owner"));
            verify(repo, never()).save(any(Owner.class));
        }
    }

    @Nested
    @DisplayName("Owner state changes")
    class OwnerStateChangeTests {

        @Test
        void shouldActivateOwnerSuccessfully() {

            // ------------------- Arrange -------------------
            Owner owner = sampleOwner("owner-1", "John Smith", "Montreal");

            when(repo.findById(OwnerId.of("owner-1")))
                    .thenReturn(Optional.of(owner));

            when(repo.save(any(Owner.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Owner activated = service.activate("owner-1");

            // ------------------- Assert -------------------
            // Business rule → ACTIVE owner is eligible
            assertThat(activated.status()).isEqualTo(OwnerStatus.ACTIVE);
            assertThat(activated.isEligibleForRegistration()).isTrue();

            verify(repo).findById(OwnerId.of("owner-1"));
            verify(repo).save(owner);
        }

        @Test
        void shouldThrowOwnerNotFoundExceptionWhenActivatingMissingOwner() {

            // ------------------- Arrange -------------------
            when(repo.findById(OwnerId.of("missing-owner")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(OwnerNotFoundException.class,
                    () -> service.activate("missing-owner"));

            verify(repo).findById(OwnerId.of("missing-owner"));
            verify(repo, never()).save(any(Owner.class));
        }

        @Test
        void shouldSuspendOwnerSuccessfully() {

            // ------------------- Arrange -------------------
            Owner owner = sampleOwner("owner-1", "John Smith", "Montreal");

            when(repo.findById(OwnerId.of("owner-1")))
                    .thenReturn(Optional.of(owner));

            when(repo.save(any(Owner.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Owner suspended = service.suspend("owner-1");

            // ------------------- Assert -------------------
            // Business rule → SUSPENDED owner is NOT eligible
            assertThat(suspended.status()).isEqualTo(OwnerStatus.SUSPENDED);
            assertThat(suspended.isEligibleForRegistration()).isFalse();

            verify(repo).findById(OwnerId.of("owner-1"));
            verify(repo).save(owner);
        }

        @Test
        void shouldThrowOwnerNotFoundExceptionWhenSuspendingMissingOwner() {

            // ------------------- Arrange -------------------
            when(repo.findById(OwnerId.of("missing-owner")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(OwnerNotFoundException.class,
                    () -> service.suspend("missing-owner"));

            verify(repo).findById(OwnerId.of("missing-owner"));
            verify(repo, never()).save(any(Owner.class));
        }
    }

    @Nested
    @DisplayName("Delete owner")
    class DeleteOwnerTests {

        @Test
        void shouldDeleteOwnerSuccessfully() {

            // ------------------- Arrange -------------------
            Owner owner = sampleOwner("owner-1", "John Smith", "Montreal");

            when(repo.findById(OwnerId.of("owner-1")))
                    .thenReturn(Optional.of(owner));

            // ------------------- Act -------------------
            service.delete("owner-1");

            // ------------------- Assert -------------------
            verify(repo).findById(OwnerId.of("owner-1"));
            verify(repo).deleteById(OwnerId.of("owner-1"));
        }

        @Test
        void shouldThrowOwnerNotFoundExceptionWhenDeletingMissingOwner() {

            // ------------------- Arrange -------------------
            when(repo.findById(OwnerId.of("missing-owner")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(OwnerNotFoundException.class,
                    () -> service.delete("missing-owner"));

            verify(repo).findById(OwnerId.of("missing-owner"));
            verify(repo, never()).deleteById(any(OwnerId.class));
        }
    }
}