-- V11: Garantía definitiva de que program_leaders existe.
-- V3 fue modificada post-apply en BDs existentes y V9 pudo no haberse ejecutado
-- si Flyway tenía bloqueado el checksum. Esta migración es idempotente (IF NOT EXISTS).
CREATE TABLE IF NOT EXISTS program_leaders (
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    PRIMARY KEY (program_id, user_id)
);
