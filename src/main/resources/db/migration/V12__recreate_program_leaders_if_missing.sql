-- V12: Garantía definitiva de program_leaders (respaldo de V11).
-- Idempotente: IF NOT EXISTS protege contra doble ejecución.
CREATE TABLE IF NOT EXISTS program_leaders (
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    PRIMARY KEY (program_id, user_id)
);
