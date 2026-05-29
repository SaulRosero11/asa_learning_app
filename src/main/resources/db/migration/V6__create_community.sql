-- V6: Comunidad — foros, posts y noticias

CREATE TABLE IF NOT EXISTS forums (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id  UUID         NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_forums_program_id ON forums(program_id);

CREATE TABLE IF NOT EXISTS forum_posts (
    id             UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    forum_id       UUID    NOT NULL REFERENCES forums(id) ON DELETE CASCADE,
    parent_post_id UUID    REFERENCES forum_posts(id) ON DELETE SET NULL,
    author_id      UUID    NOT NULL REFERENCES users(id),
    content        TEXT    NOT NULL,
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_forum_posts_forum_id    ON forum_posts(forum_id);
CREATE INDEX idx_forum_posts_parent_id   ON forum_posts(parent_post_id) WHERE parent_post_id IS NOT NULL;
CREATE INDEX idx_forum_posts_author_id   ON forum_posts(author_id);

CREATE TABLE IF NOT EXISTS news (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    program_id   UUID         NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    author_id    UUID         NOT NULL REFERENCES users(id),
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at   TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_news_program_id ON news(program_id);
CREATE INDEX idx_news_published_at ON news(published_at DESC);
