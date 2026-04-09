CREATE TABLE users
(
    id         BINARY(16)   NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    full_name  VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    `role`     VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

CREATE UNIQUE INDEX idx_user_email ON users (email);