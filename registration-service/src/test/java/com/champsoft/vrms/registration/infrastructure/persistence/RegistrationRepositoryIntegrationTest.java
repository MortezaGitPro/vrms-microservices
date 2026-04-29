package com.champsoft.vrms.registration.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

// @DataJpaTest → loads only JPA layer (entities + repositories)
// Automatically starts an in-memory database (H2)
@DataJpaTest

// Use testing profile → ensures we use H2 instead of PostgreSQL
@ActiveProfiles("testing")
class RegistrationRepositoryIntegrationTest {

    // Real repository (NOT mocked)
    // This is an integration test → we verify real database behavior
    @Autowired
    private SpringDataRegistrationRepository repository;

    @Test
    @DisplayName("Should save a registration successfully")
    void shouldSaveRegistrationSuccessfully() {

        // ------------------- Arrange -------------------
        // Create JPA entity (this is persistence model, not domain model)
        RegistrationJpaEntity registration = new RegistrationJpaEntity();

        // IDs are stored as simple strings (no foreign keys across services)
        registration.id = "reg-1";
        registration.vehicleId = "vehicle-1";
        registration.ownerId = "owner-1";
        registration.agentId = "agent-1";

        // Business data
        registration.plate = "ABC123";

        // Expiry date stored as LocalDate
        registration.expiry = LocalDate.now().plusYears(1);

        // Status stored as String in DB
        registration.status = "ACTIVE";

        // ------------------- Act -------------------
        // Save entity to database (H2)
        RegistrationJpaEntity saved = repository.save(registration);

        // ------------------- Assert -------------------
        // Verify persistence worked correctly
        assertThat(saved).isNotNull();
        assertThat(saved.id).isEqualTo("reg-1");

        // Verify IDs
        assertThat(saved.vehicleId).isEqualTo("vehicle-1");
        assertThat(saved.ownerId).isEqualTo("owner-1");
        assertThat(saved.agentId).isEqualTo("agent-1");

        // Verify business fields
        assertThat(saved.plate).isEqualTo("ABC123");
        assertThat(saved.expiry).isEqualTo(LocalDate.now().plusYears(1));
        assertThat(saved.status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should find a registration by plate")
    void shouldFindRegistrationByPlate() {

        // ------------------- Arrange -------------------
        RegistrationJpaEntity registration = new RegistrationJpaEntity();
        registration.id = "reg-2";
        registration.vehicleId = "vehicle-2";
        registration.ownerId = "owner-2";
        registration.agentId = "agent-2";
        registration.plate = "XYZ789";
        registration.expiry = LocalDate.now().plusYears(1);
        registration.status = "ACTIVE";

        // Must save before querying
        repository.save(registration);

        // ------------------- Act -------------------
        Optional<RegistrationJpaEntity> found = repository.findByPlate("XYZ789");

        // ------------------- Assert -------------------
        // Verify record exists and values are correct
        assertThat(found).isPresent();

        assertThat(found.get().id).isEqualTo("reg-2");
        assertThat(found.get().vehicleId).isEqualTo("vehicle-2");
        assertThat(found.get().ownerId).isEqualTo("owner-2");
        assertThat(found.get().agentId).isEqualTo("agent-2");

        assertThat(found.get().plate).isEqualTo("XYZ789");
        assertThat(found.get().status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should return true when plate exists")
    void shouldReturnTrueWhenPlateExists() {

        // ------------------- Arrange -------------------
        RegistrationJpaEntity registration = new RegistrationJpaEntity();
        registration.id = "reg-3";
        registration.vehicleId = "vehicle-3";
        registration.ownerId = "owner-3";
        registration.agentId = "agent-3";
        registration.plate = "PLATE123";
        registration.expiry = LocalDate.now().plusYears(1);
        registration.status = "ACTIVE";

        repository.save(registration);

        // ------------------- Act -------------------
        boolean exists = repository.existsByPlate("PLATE123");

        // ------------------- Assert -------------------
        // Plate exists → should return true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when plate does not exist")
    void shouldReturnFalseWhenPlateDoesNotExist() {

        // ------------------- Act -------------------
        boolean exists = repository.existsByPlate("NOT-EXISTING");

        // ------------------- Assert -------------------
        // Plate not in DB → should return false
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should delete a registration successfully")
    void shouldDeleteRegistrationSuccessfully() {

        // ------------------- Arrange -------------------
        RegistrationJpaEntity registration = new RegistrationJpaEntity();
        registration.id = "reg-4";
        registration.vehicleId = "vehicle-4";
        registration.ownerId = "owner-4";
        registration.agentId = "agent-4";
        registration.plate = "DELETE123";
        registration.expiry = LocalDate.now().plusYears(1);
        registration.status = "ACTIVE";

        repository.save(registration);

        // ------------------- Act -------------------
        // Delete from database
        repository.deleteById("reg-4");

        // Try to find again
        Optional<RegistrationJpaEntity> found = repository.findById("reg-4");

        // ------------------- Assert -------------------
        // Entity should no longer exist
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when plate is not found")
    void shouldReturnEmptyWhenPlateIsNotFound() {

        // ------------------- Act -------------------
        Optional<RegistrationJpaEntity> found = repository.findByPlate("UNKNOWN-PLATE");

        // ------------------- Assert -------------------
        // No record → empty result
        assertThat(found).isEmpty();
    }
}