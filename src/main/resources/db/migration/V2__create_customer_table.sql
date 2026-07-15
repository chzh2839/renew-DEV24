CREATE TABLE customer (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(100),
    nickname        VARCHAR(50),
    email           VARCHAR(100),
    phone           VARCHAR(20),
    address         VARCHAR(255),
    interest        VARCHAR(255),
    newsletter_yn   BOOLEAN      NOT NULL DEFAULT FALSE,
    joined_at       TIMESTAMP    NOT NULL,
    point           INT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_customer_login_id UNIQUE (login_id)
);
