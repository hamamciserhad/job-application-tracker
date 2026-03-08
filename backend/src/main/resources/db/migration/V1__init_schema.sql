-- Users table
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Applications table
CREATE TABLE applications (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    position     VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'APPLIED',
    salary       NUMERIC(15, 2),
    location     VARCHAR(255),
    job_url      TEXT,
    notes        TEXT,
    applied_date DATE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_applications_user_id ON applications(user_id);
CREATE INDEX idx_applications_status  ON applications(status);

-- Status history table
CREATE TABLE status_history (
    id             BIGSERIAL   PRIMARY KEY,
    application_id BIGINT      NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    old_status     VARCHAR(50),
    new_status     VARCHAR(50) NOT NULL,
    changed_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    notes          TEXT
);

CREATE INDEX idx_status_history_application_id ON status_history(application_id);
