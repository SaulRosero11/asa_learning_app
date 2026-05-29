-- V4: Módulos y lecciones del contenido e-learning

CREATE TABLE IF NOT EXISTS modules (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id  UUID         NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    order_index INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_modules_program_id ON modules(program_id);

CREATE TABLE IF NOT EXISTS lessons (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id    UUID         NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    content_type VARCHAR(50)  NOT NULL DEFAULT 'TEXT',
    content_url  TEXT,
    order_index  INTEGER      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lessons_module_id ON lessons(module_id);

-- Tracking de lecciones completadas por estudiante
CREATE TABLE IF NOT EXISTS lesson_completions (
    lesson_id    UUID NOT NULL REFERENCES lessons(id)  ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (lesson_id, user_id)
);

CREATE INDEX idx_lesson_completions_user_id ON lesson_completions(user_id);
