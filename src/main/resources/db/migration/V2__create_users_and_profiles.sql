-- V2: Usuarios, roles de usuario y perfiles

CREATE TABLE IF NOT EXISTS users (
    id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255),
    auth_provider         VARCHAR(50)  NOT NULL DEFAULT 'INTERNAL',
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    is_admin_eligible     BOOLEAN      NOT NULL DEFAULT FALSE,
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    onboarding_completed  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at            TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id          UUID         PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    phone_number     VARCHAR(20),
    demographic_data JSONB        NOT NULL DEFAULT '{}',
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_profiles_demographic ON user_profiles USING GIN (demographic_data);
