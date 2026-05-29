-- V5: Motor de evaluaciones (assessments, preguntas, opciones, intentos, respuestas)

CREATE TABLE IF NOT EXISTS assessments (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id   UUID         NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    module_id    UUID         REFERENCES modules(id) ON DELETE SET NULL,
    title        VARCHAR(255) NOT NULL,
    type         VARCHAR(50)  NOT NULL DEFAULT 'EXAM',
    is_published BOOLEAN      NOT NULL DEFAULT FALSE,
    start_date   TIMESTAMP WITH TIME ZONE,
    end_date     TIMESTAMP WITH TIME ZONE,
    max_attempts INTEGER      NOT NULL DEFAULT 1,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_assessments_program_id   ON assessments(program_id);
CREATE INDEX idx_assessments_is_published ON assessments(is_published);

CREATE TABLE IF NOT EXISTS questions (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID        NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    question_text TEXT        NOT NULL,
    question_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE_CHOICE',
    weight        DECIMAL(5,2) NOT NULL DEFAULT 0,
    order_index   INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_questions_assessment_id ON questions(assessment_id);

CREATE TABLE IF NOT EXISTS question_options (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID    NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    option_text TEXT    NOT NULL,
    is_correct  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_question_options_question_id ON question_options(question_id);

CREATE TABLE IF NOT EXISTS assessment_attempts (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID        NOT NULL REFERENCES assessments(id),
    user_id       UUID        NOT NULL REFERENCES users(id),
    attempt_number INTEGER    NOT NULL DEFAULT 1,
    start_time    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    end_time      TIMESTAMP WITH TIME ZONE,
    total_score   DECIMAL(5,2) NOT NULL DEFAULT 0,
    status        VARCHAR(50)  NOT NULL DEFAULT 'IN_PROGRESS'
);

CREATE INDEX idx_attempts_user_assessment ON assessment_attempts(user_id, assessment_id);
CREATE INDEX idx_attempts_assessment_id   ON assessment_attempts(assessment_id);

CREATE TABLE IF NOT EXISTS student_responses (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id         UUID         NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    question_id        UUID         NOT NULL REFERENCES questions(id),
    selected_option_id UUID         REFERENCES question_options(id),
    text_response      TEXT,
    points_awarded     DECIMAL(5,2) NOT NULL DEFAULT 0
);

CREATE INDEX idx_student_responses_attempt_id ON student_responses(attempt_id);
