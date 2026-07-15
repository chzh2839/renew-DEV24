CREATE TABLE email_notification_history (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    to_email        VARCHAR(100) NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    body            TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    error_message   TEXT,
    sent_at         TIMESTAMP    NOT NULL
);
