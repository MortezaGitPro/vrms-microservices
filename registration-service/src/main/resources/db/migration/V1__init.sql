-- Flyway init schema for REGISTRATION SERVICE ONLY
-- This service owns the registrations table only.
-- vehicle_id, owner_id, and agent_id are external references to other services.
-- No cross-service foreign keys are used.

CREATE TABLE IF NOT EXISTS registrations (
    id VARCHAR(36) PRIMARY KEY,
    vehicle_id VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    agent_id VARCHAR(36) NOT NULL,
    plate VARCHAR(16) NOT NULL UNIQUE,
    expiry DATE NOT NULL,
    status VARCHAR(20) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_registrations_vehicle_id ON registrations(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_registrations_owner_id ON registrations(owner_id);
CREATE INDEX IF NOT EXISTS idx_registrations_agent_id ON registrations(agent_id);

INSERT INTO registrations (id, vehicle_id, owner_id, agent_id, plate, expiry, status) VALUES
    ('reg-1', 'veh-1', 'own-1', 'agent-1', 'QC-1001', DATE '2027-12-31', 'ACTIVE'),
    ('reg-2', 'veh-2', 'own-2', 'agent-1', 'QC-1002', DATE '2028-11-30', 'ACTIVE'),
    ('reg-3', 'veh-3', 'own-3', 'agent-1', 'QC-1003', DATE '2028-10-31', 'ACTIVE'),
    ('reg-4', 'veh-4', 'own-4', 'agent-1', 'QC-1004', DATE '2028-09-30', 'ACTIVE'),
    ('reg-5', 'veh-5', 'own-5', 'agent-1', 'QC-1005', DATE '2028-08-31', 'ACTIVE'),
    ('reg-6', 'veh-6', 'own-6', 'agent-2', 'QC-1006', DATE '2026-07-31', 'ACTIVE'),
    ('reg-7', 'veh-7', 'own-7', 'agent-2', 'QC-1007', DATE '2026-06-30', 'ACTIVE'),
    ('reg-8', 'veh-8', 'own-8', 'agent-2', 'QC-1008', DATE '2026-05-31', 'ACTIVE'),
    ('reg-9', 'veh-9', 'own-9', 'agent-2', 'QC-1009', DATE '2026-04-30', 'ACTIVE'),
    ('reg-10','veh-10','own-10','agent-2','QC-1010', DATE '2026-03-31', 'ACTIVE');