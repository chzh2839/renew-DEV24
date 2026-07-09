CREATE TABLE admin (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100),
    CONSTRAINT uk_admin_login_id UNIQUE (login_id)
);
