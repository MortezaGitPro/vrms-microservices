-- Flyway init schema for OWNERS SERVICE ONLY
-- This service owns the owners table

CREATE TABLE IF NOT EXISTS owners (
    id VARCHAR(36) PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    address VARCHAR(200),
    status VARCHAR(20) NOT NULL
);

INSERT INTO owners (id, full_name, address, status) VALUES
    ('own-1', 'John Smith', 'Montreal, QC', 'ACTIVE'),
    ('own-2', 'Sarah Johnson', 'Laval, QC', 'ACTIVE'),
    ('own-3', 'Michael Brown', 'Longueuil, QC', 'ACTIVE'),
    ('own-4', 'Emily Davis', 'Brossard, QC', 'ACTIVE'),
    ('own-5', 'David Wilson', 'Montreal, QC', 'ACTIVE'),
    ('own-6', 'Laura Martinez', 'Laval, QC', 'ACTIVE'),
    ('own-7', 'James Anderson', 'Montreal, QC', 'ACTIVE'),
    ('own-8', 'Olivia Taylor', 'Longueuil, QC', 'ACTIVE'),
    ('own-9', 'Daniel Thomas', 'Brossard, QC', 'ACTIVE'),
    ('own-10','Sophia Moore', 'Montreal, QC', 'ACTIVE');
