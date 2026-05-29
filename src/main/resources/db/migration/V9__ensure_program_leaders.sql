-- Garantiza que la tabla program_leaders exista.
-- Necesaria porque V3 fue modificada después de haber sido aplicada en bases de datos existentes.
CREATE TABLE IF NOT EXISTS program_leaders (
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    PRIMARY KEY (program_id, user_id)
);
