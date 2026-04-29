package com.champsoft.vrms.cars.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

// @DataJpaTest → loads only JPA components (repositories, entities)
// It starts an in-memory database (H2) automatically
@DataJpaTest

// Use testing profile → ensures we use H2 instead of PostgreSQL
@ActiveProfiles("testing")
class VehicleRepositoryIntegrationTest {

    // Real repository (NOT mocked)
    // This is an integration test → we test real database behavior
    @Autowired
    private SpringDataVehicleRepository repository;

    @Test
    @DisplayName("Should save a vehicle successfully")
    void shouldSaveVehicleSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a JPA entity (this is NOT the domain model)
        // This represents how data is stored in the database
        VehicleJpaEntity vehicle = new VehicleJpaEntity();
        vehicle.id = "car-1";
        vehicle.vin = "1HGCM82633A123456";

        // Embedded object (maps to multiple DB columns)
        vehicle.specs = new VehicleSpecsEmbeddable("Toyota", "Corolla", 2020);

        // Stored as String in DB
        vehicle.status = "ACTIVE";

        // ------------------- Act -------------------
        // Save to database (H2)
        VehicleJpaEntity saved = repository.save(vehicle);

        // ------------------- Assert -------------------
        // Verify entity was persisted correctly
        assertThat(saved).isNotNull();
        assertThat(saved.id).isEqualTo("car-1");
        assertThat(saved.vin).isEqualTo("1HGCM82633A123456");

        // Verify embedded object mapping
        assertThat(saved.specs).isNotNull();
        assertThat(saved.specs.make).isEqualTo("Toyota");
        assertThat(saved.specs.model).isEqualTo("Corolla");
        assertThat(saved.specs.year).isEqualTo(2020);

        // Verify status persisted correctly
        assertThat(saved.status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should find a vehicle by VIN")
    void shouldFindVehicleByVin() {

        // ------------------- Arrange -------------------
        VehicleJpaEntity vehicle = new VehicleJpaEntity();
        vehicle.id = "car-2";
        vehicle.vin = "2HGCM82633A123456";
        vehicle.specs = new VehicleSpecsEmbeddable("Honda", "Civic", 2021);
        vehicle.status = "ACTIVE";

        // Save first → needed before querying
        repository.save(vehicle);

        // ------------------- Act -------------------
        Optional<VehicleJpaEntity> found = repository.findByVin("2HGCM82633A123456");

        // ------------------- Assert -------------------
        // Verify record exists and values are correct
        assertThat(found).isPresent();
        assertThat(found.get().id).isEqualTo("car-2");
        assertThat(found.get().vin).isEqualTo("2HGCM82633A123456");

        assertThat(found.get().specs.make).isEqualTo("Honda");
        assertThat(found.get().specs.model).isEqualTo("Civic");
        assertThat(found.get().specs.year).isEqualTo(2021);

        assertThat(found.get().status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should return true when VIN exists")
    void shouldReturnTrueWhenVinExists() {

        // ------------------- Arrange -------------------
        VehicleJpaEntity vehicle = new VehicleJpaEntity();
        vehicle.id = "car-3";
        vehicle.vin = "3HGCM82633A123456";
        vehicle.specs = new VehicleSpecsEmbeddable("Mazda", "Mazda3", 2022);
        vehicle.status = "ACTIVE";

        repository.save(vehicle);

        // ------------------- Act -------------------
        boolean exists = repository.existsByVin("3HGCM82633A123456");

        // ------------------- Assert -------------------
        // VIN exists → should return true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when VIN does not exist")
    void shouldReturnFalseWhenVinDoesNotExist() {

        // ------------------- Act -------------------
        boolean exists = repository.existsByVin("NOT-EXISTING-VIN");

        // ------------------- Assert -------------------
        // VIN not in DB → should return false
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should delete a vehicle successfully")
    void shouldDeleteVehicleSuccessfully() {

        // ------------------- Arrange -------------------
        VehicleJpaEntity vehicle = new VehicleJpaEntity();
        vehicle.id = "car-4";
        vehicle.vin = "4HGCM82633A123456";
        vehicle.specs = new VehicleSpecsEmbeddable("Ford", "Focus", 2020);
        vehicle.status = "ACTIVE";

        repository.save(vehicle);

        // ------------------- Act -------------------
        // Delete from database
        repository.deleteById("car-4");

        // Try to retrieve again
        Optional<VehicleJpaEntity> found = repository.findById("car-4");

        // ------------------- Assert -------------------
        // Entity should no longer exist
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when VIN is not found")
    void shouldReturnEmptyWhenVinIsNotFound() {

        // ------------------- Act -------------------
        Optional<VehicleJpaEntity> found = repository.findByVin("UNKNOWN-VIN");

        // ------------------- Assert -------------------
        // No record → empty Optional
        assertThat(found).isEmpty();
    }
}