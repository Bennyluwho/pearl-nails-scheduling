CREATE TABLE database_health_check (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(150) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO database_health_check (message)
VALUES ('PostgreSQL and Flyway are connected');