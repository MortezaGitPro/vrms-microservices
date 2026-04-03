-- Flyway init schema for AGENTS SERVICE ONLY
-- This service owns the agents table

CREATE TABLE IF NOT EXISTS agents (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);

INSERT INTO agents (id, name, role, status) VALUES
    ('agent-1', 'Alice Martin', 'CLERK', 'ACTIVE'),
    ('agent-2', 'Robert Lee', 'SUPERVISOR', 'ACTIVE');
