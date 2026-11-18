CREATE TYPE storage_type AS ENUM ('STAGING', 'PERMANENT');

CREATE TABLE storages (
    id BIGSERIAL PRIMARY KEY,
    storage_type storage_type NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL
);

-- -- Insert example records
-- INSERT INTO storages (storage_type, bucket, path) 
-- VALUES 
--     ('STAGING', 'staging-mp3', 'files/'),
--     ('PERMANENT', 'permanent-mp3', 'files/'),