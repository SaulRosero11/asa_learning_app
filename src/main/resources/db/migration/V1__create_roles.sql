-- V1: Roles del sistema
CREATE TABLE IF NOT EXISTS roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description TEXT
);

INSERT INTO roles (name, description) VALUES
    ('STUDENT',        'Beneficiario matriculado en programas de capacitación'),
    ('PROGRAM_LEADER', 'Coordinador responsable de un programa específico'),
    ('ADMIN',          'Administrador con acceso a todos los programas'),
    ('SUPER_ADMIN',    'Administrador global con acceso total al sistema');
