CREATE TABLE refresh_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token      TEXT        NOT NULL UNIQUE,
    username   VARCHAR(64) NOT NULL,
    issued_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ
);
CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked);
