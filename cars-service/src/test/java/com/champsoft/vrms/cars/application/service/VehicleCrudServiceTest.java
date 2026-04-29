package com.champsoft.vrms.cars.application.service;

import com.champsoft.vrms.cars.application.exception.DuplicateVinException;
import com.champsoft.vrms.cars.application.exception.VehicleNotFoundException;
import com.champsoft.vrms.cars.application.port.out.VehicleRepositoryPort;
import com.champsoft.vrms.cars.domain.model.Vehicle;
import com.champsoft.vrms.cars.domain.model.VehicleId;
import com.champsoft.vrms.cars.domain.model.VehicleSpecs;
import com.champsoft.vrms.cars.domain.model.VehicleStatus;
import com.champsoft.vrms.cars.domain.model.Vin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Service-layer unit test → tests application logic only
// NO Spring Boot context, NO real database
// Mockito is used to fake the repository dependency
@ExtendWith(MockitoExtension.class)
class VehicleCrudServiceTest {

    // Mock repository:
    // This fake object replaces the real database/repository.
    @Mock
    private VehicleRepositoryPort repo;

    // InjectMocks:
    // Mockito creates VehicleCrudService and injects the mocked repo into it.
    @InjectMocks
    private VehicleCrudService service;

    @Nested
    @DisplayName("Create vehicle")
    class CreateVehicleTests {

        @Test
        void shouldCreateVehicleSuccessfully() {

            // ------------------- Arrange -------------------
            // Create the VIN value object that the service is expected to use.
            Vin vin = new Vin("1HGCM82633A123456");

            // Tell the mock repository that this VIN does not already exist.
            // This allows the service to create the vehicle.
            when(repo.existsByVin(vin)).thenReturn(false);

            // When the service saves a vehicle, return the same vehicle object.
            // This simulates a successful save without using a real database.
            when(repo.save(any(Vehicle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            // Call the service method being tested.
            Vehicle result = service.create("1HGCM82633A123456", "Toyota", "Corolla", 2020);

            // ------------------- Assert -------------------
            // Verify that a vehicle was created.
            assertThat(result).isNotNull();

            // Verify that the created vehicle has the expected VIN.
            assertThat(result.vin().value()).isEqualTo("1HGCM82633A123456");

            // Verify that the created vehicle has the expected specs.
            assertThat(result.specs().make()).isEqualTo("Toyota");
            assertThat(result.specs().model()).isEqualTo("Corolla");
            assertThat(result.specs().year()).isEqualTo(2020);

            // Business rule:
            // A newly created vehicle should start as INACTIVE.
            assertThat(result.status()).isEqualTo(VehicleStatus.INACTIVE);

            // Verify that the service checked if the VIN already existed.
            verify(repo).existsByVin(vin);

            // Verify that the service saved the new vehicle.
            verify(repo).save(any(Vehicle.class));
        }

        @Test
        void shouldThrowDuplicateVinExceptionWhenVinAlreadyExists() {

            // ------------------- Arrange -------------------
            // Create the VIN value object that already exists.
            Vin vin = new Vin("1HGCM82633A123456");

            // Tell the mock repository that this VIN already exists.
            when(repo.existsByVin(vin)).thenReturn(true);

            // ------------------- Act + Assert -------------------
            // Business rule:
            // The system must not create two vehicles with the same VIN.
            assertThrows(DuplicateVinException.class,
                    () -> service.create("1HGCM82633A123456", "Toyota", "Corolla", 2020));

            // Verify that the service checked the VIN.
            verify(repo).existsByVin(vin);

            // Since the VIN already exists, the vehicle must not be saved.
            verify(repo, never()).save(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("Read vehicle")
    class ReadVehicleTests {

        @Test
        void shouldReturnVehicleWhenGettingById() {

            // ------------------- Arrange -------------------
            // Create a sample vehicle for the repository to return.
            Vehicle vehicle = sampleVehicle();

            // Tell the repository mock to return the vehicle when this ID is searched.
            when(repo.findById(VehicleId.of("car-1"))).thenReturn(Optional.of(vehicle));

            // ------------------- Act -------------------
            // Ask the service to find the vehicle by ID.
            Vehicle result = service.getById("car-1");

            // ------------------- Assert -------------------
            // Verify that the returned vehicle has the expected ID.
            assertThat(result.id().value()).isEqualTo("car-1");

            // Verify that the service called the repository with the correct ID.
            verify(repo).findById(VehicleId.of("car-1"));
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenGettingMissingVehicleById() {

            // ------------------- Arrange -------------------
            // Tell the repository mock that no vehicle exists with this ID.
            when(repo.findById(VehicleId.of("missing"))).thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            // Business rule:
            // If the vehicle does not exist, the service should throw VehicleNotFoundException.
            assertThrows(VehicleNotFoundException.class,
                    () -> service.getById("missing"));
        }

        @Test
        void shouldReturnVehicleWhenGettingByVin() {

            // ------------------- Arrange -------------------
            // Create a sample vehicle.
            Vehicle vehicle = sampleVehicle();

            // Create the VIN value object used for the search.
            Vin vin = new Vin("1HGCM82633A123456");

            // Tell the repository mock to return the vehicle for this VIN.
            when(repo.findByVin(vin)).thenReturn(Optional.of(vehicle));

            // ------------------- Act -------------------
            // Ask the service to find the vehicle by VIN.
            Vehicle result = service.getByVin("1HGCM82633A123456");

            // ------------------- Assert -------------------
            // Verify that the returned vehicle has the expected VIN.
            assertThat(result.vin().value()).isEqualTo("1HGCM82633A123456");

            // Verify that the service called the repository with the correct VIN.
            verify(repo).findByVin(vin);
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenGettingMissingVehicleByVin() {

            // ------------------- Arrange -------------------
            // Create the VIN value object used for the search.
            Vin vin = new Vin("1HGCM82633A123456");

            // Tell the repository mock that no vehicle exists with this VIN.
            when(repo.findByVin(vin)).thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            // Business rule:
            // If the VIN is not found, the service should throw VehicleNotFoundException.
            assertThrows(VehicleNotFoundException.class,
                    () -> service.getByVin("1HGCM82633A123456"));
        }

        @Test
        void shouldReturnAllVehicles() {

            // ------------------- Arrange -------------------
            // Tell the repository mock to return a list with one sample vehicle.
            when(repo.findAll()).thenReturn(List.of(sampleVehicle()));

            // ------------------- Act -------------------
            // Ask the service to list all vehicles.
            List<Vehicle> result = service.list();

            // ------------------- Assert -------------------
            // Verify that the service returns one vehicle.
            assertThat(result).hasSize(1);

            // Verify that the service called the repository list method.
            verify(repo).findAll();
        }
    }

    @Nested
    @DisplayName("Update vehicle")
    class UpdateVehicleTests {

        @Test
        void shouldUpdateVehicleSpecsSuccessfully() {

            // ------------------- Arrange -------------------
            // Create a sample vehicle with original specs.
            Vehicle vehicle = sampleVehicle();

            // Tell the repository mock to find this vehicle by ID.
            when(repo.findById(VehicleId.of("car-1"))).thenReturn(Optional.of(vehicle));

            // Simulate saving the updated vehicle.
            when(repo.save(any(Vehicle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            // Update the vehicle specs through the service.
            Vehicle result = service.updateSpecs("car-1", "Honda", "Civic", 2022);

            // ------------------- Assert -------------------
            // Verify that the vehicle specs were updated correctly.
            assertThat(result.specs()).isEqualTo(new VehicleSpecs("Honda", "Civic", 2022));

            // Verify that the updated vehicle was saved.
            verify(repo).save(vehicle);
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenUpdatingMissingVehicle() {

            // ------------------- Arrange -------------------
            // Tell the repository mock that the vehicle does not exist.
            when(repo.findById(VehicleId.of("missing"))).thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            // Business rule:
            // A missing vehicle cannot be updated.
            assertThrows(VehicleNotFoundException.class,
                    () -> service.updateSpecs("missing", "Honda", "Civic", 2022));

            // Since the vehicle was not found, save should never be called.
            verify(repo, never()).save(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("Activate vehicle")
    class ActivateVehicleTests {

        @Test
        void shouldActivateVehicleSuccessfully() {

            // ------------------- Arrange -------------------
            // Create a sample vehicle.
            // By default, it starts as INACTIVE.
            Vehicle vehicle = sampleVehicle();

            // Tell the repository mock to find this vehicle by ID.
            when(repo.findById(VehicleId.of("car-1"))).thenReturn(Optional.of(vehicle));

            // Simulate saving the activated vehicle.
            when(repo.save(any(Vehicle.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            // Activate the vehicle through the service.
            Vehicle result = service.activate("car-1");

            // ------------------- Assert -------------------
            // Business rule:
            // After activation, the vehicle status should be ACTIVE.
            assertThat(result.status()).isEqualTo(VehicleStatus.ACTIVE);

            // Verify that the activated vehicle was saved.
            verify(repo).save(vehicle);
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenActivatingMissingVehicle() {

            // ------------------- Arrange -------------------
            // Tell the repository mock that the vehicle does not exist.
            when(repo.findById(VehicleId.of("missing"))).thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            // Business rule:
            // A missing vehicle cannot be activated.
            assertThrows(VehicleNotFoundException.class,
                    () -> service.activate("missing"));

            // Since the vehicle was not found, save should never be called.
            verify(repo, never()).save(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("Delete vehicle")
    class DeleteVehicleTests {

        @Test
        void shouldDeleteVehicleSuccessfully() {

            // ------------------- Arrange -------------------
            // Tell the repository mock that the vehicle exists.
            when(repo.findById(VehicleId.of("car-1"))).thenReturn(Optional.of(sampleVehicle()));

            // ------------------- Act -------------------
            // Delete the vehicle through the service.
            service.delete("car-1");

            // ------------------- Assert -------------------
            // Verify that the repository deleted the vehicle by ID.
            verify(repo).deleteById(VehicleId.of("car-1"));
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenDeletingMissingVehicle() {

            // ------------------- Arrange -------------------
            // Tell the repository mock that the vehicle does not exist.
            when(repo.findById(VehicleId.of("missing"))).thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            // Business rule:
            // A missing vehicle cannot be deleted.
            assertThrows(VehicleNotFoundException.class,
                    () -> service.delete("missing"));

            // Since the vehicle was not found, delete should never be called.
            verify(repo, never()).deleteById(any(VehicleId.class));
        }
    }

    // Helper method:
    // Creates a reusable sample vehicle for the tests above.
    // This avoids repeating the same object creation code many times.
    private Vehicle sampleVehicle() {
        return new Vehicle(
                VehicleId.of("car-1"),
                new Vin("1HGCM82633A123456"),
                new VehicleSpecs("Toyota", "Corolla", 2020)
        );
    }
}