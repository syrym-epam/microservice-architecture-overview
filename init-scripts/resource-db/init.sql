CREATE TABLE entity_resource (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255),
    path VARCHAR(255),
    storage_type VARCHAR(50) NOT NULL,
    CONSTRAINT chk_storage_type CHECK (storage_type IN ('STAGING', 'PERMANENT'))
);