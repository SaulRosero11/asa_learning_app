-- V3: Programas, líderes, matrículas e invitaciones

CREATE TABLE IF NOT EXISTS programs (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    start_date  TIMESTAMP WITH TIME ZONE,
    end_date    TIMESTAMP WITH TIME ZONE,
    created_by  UUID         REFERENCES users(id),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_programs_status ON programs(status);
CREATE INDEX idx_programs_created_by ON programs(created_by);

CREATE TABLE IF NOT EXISTS program_leaders (
    program_id UUID NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    PRIMARY KEY (program_id, user_id)
);

CREATE TABLE IF NOT EXISTS enrollments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id      UUID        NOT NULL REFERENCES programs(id),
    user_id         UUID        NOT NULL REFERENCES users(id),
    status          VARCHAR(50) NOT NULL DEFAULT 'ENROLLED',
    enrolled_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completion_date TIMESTAMP WITH TIME ZONE,
    final_grade     DECIMAL(5,2),
    UNIQUE (program_id, user_id)
);

CREATE INDEX idx_enrollments_user_id    ON enrollments(user_id);
CREATE INDEX idx_enrollments_program_id ON enrollments(program_id);
CREATE INDEX idx_enrollments_status     ON enrollments(status);

CREATE TABLE IF NOT EXISTS invitations (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id UUID         NOT NULL REFERENCES programs(id),
    email      VARCHAR(255) NOT NULL,
    token      VARCHAR(255) NOT NULL UNIQUE,
    status     VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID         REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invitations_token  ON invitations(token);
CREATE INDEX idx_invitations_email  ON invitations(email);
CREATE INDEX idx_invitations_status ON invitations(status);
