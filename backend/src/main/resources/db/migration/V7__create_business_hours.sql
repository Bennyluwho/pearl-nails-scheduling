CREATE TABLE business_hours (
    id BIGSERIAL PRIMARY KEY,

    day_of_week SMALLINT NOT NULL,
    open_time TIME WITHOUT TIME ZONE,
    close_time TIME WITHOUT TIME ZONE,

    closed BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT business_hours_day_of_week_range
        CHECK (day_of_week BETWEEN 1 AND 7),

    CONSTRAINT business_hours_day_unique
        UNIQUE (day_of_week),

    CONSTRAINT business_hours_valid_times
        CHECK (
            (
                closed = TRUE
                AND open_time IS NULL
                AND close_time IS NULL
            )
            OR
            (
                closed = FALSE
                AND open_time IS NOT NULL
                AND close_time IS NOT NULL
                AND open_time < close_time
            )
        )
);