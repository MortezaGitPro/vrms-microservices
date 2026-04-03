-- Flyway init schema for CARS SERVICE ONLY
-- This service owns the vehicles table

CREATE TABLE IF NOT EXISTS vehicles (
    id VARCHAR(36) PRIMARY KEY,
    vin VARCHAR(32) NOT NULL UNIQUE,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    vehicle_year INT NOT NULL,
    status VARCHAR(20) NOT NULL
);

INSERT INTO vehicles (id, vin, make, model, vehicle_year, status) VALUES
    ('veh-1', 'VIN0001ABCDEFGHJK', 'Toyota', 'Corolla', 2018, 'ACTIVE'),
    ('veh-2', 'VIN0002ABCDEFGHJK', 'Honda', 'Civic', 2019, 'ACTIVE'),
    ('veh-3', 'VIN0003ABCDEFGHJK', 'Ford', 'Focus', 2017, 'ACTIVE'),
    ('veh-4', 'VIN0004ABCDEFGHJK', 'BMW', '320i', 2020, 'ACTIVE'),
    ('veh-5', 'VIN0005ABCDEFGHJK', 'Audi', 'A4', 2021, 'ACTIVE'),
    ('veh-6', 'VIN0006ABCDEFGHJK', 'Hyundai', 'Elantra', 2018, 'ACTIVE'),
    ('veh-7', 'VIN0007ABCDEFGHJK', 'Kia', 'Forte', 2019, 'ACTIVE'),
    ('veh-8', 'VIN0008ABCDEFGHJK', 'Mazda', 'Mazda3', 2020, 'ACTIVE'),
    ('veh-9', 'VIN0009ABCDEFGHJK', 'Nissan', 'Sentra', 2017, 'ACTIVE'),
    ('veh-10','VIN0010ABCDEFGHJK', 'Volkswagen', 'Jetta', 2021, 'ACTIVE');