-- Tabla de preguntas configurables del onboarding
CREATE TABLE onboarding_questions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label         VARCHAR(500) NOT NULL,
    field_key     VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NOT NULL CHECK (type IN ('TEXT', 'NUMBER', 'SELECT')),
    options       JSONB,
    display_order INT          NOT NULL DEFAULT 0,
    required      BOOLEAN      NOT NULL DEFAULT true,
    active        BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_onboarding_field_key UNIQUE (field_key)
);

CREATE INDEX idx_onboarding_questions_order  ON onboarding_questions (display_order);
CREATE INDEX idx_onboarding_questions_active ON onboarding_questions (active);

-- Seed: las 5 preguntas existentes (field_key debe coincidir con las claves ya usadas en demographic_data)
INSERT INTO onboarding_questions (label, field_key, type, options, display_order, required, active) VALUES
(
    'Edad',
    'age',
    'NUMBER',
    NULL,
    1,
    true,
    true
),
(
    'Ocupación',
    'occupation',
    'SELECT',
    '["Estudiante", "Empleado dependiente", "Emprendedor / Negocio propio", "Desempleado", "Otro"]',
    2,
    true,
    true
),
(
    'Nivel de educación',
    'educationLevel',
    'SELECT',
    '["Primaria", "Secundaria", "Técnico / Tecnólogo", "Universidad en curso", "Universitario graduado", "Posgrado"]',
    3,
    true,
    true
),
(
    'Ciudad / Localidad',
    'location',
    'TEXT',
    NULL,
    4,
    true,
    true
),
(
    '¿Cómo conociste ASA?',
    'howDidYouHear',
    'TEXT',
    NULL,
    5,
    false,
    true
);
