
CREATE TABLE employee_time_off (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    time_off_date DATE NOT NULL,

    full_day BOOLEAN NOT NULL DEFAULT FALSE,

    start_time TIME WITHOUT TIME ZONE,
    end_time TIME WITHOUT TIME ZONE,

    reason VARCHAR(255),

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT employee_time_off_employee_fk
        FOREIGN KEY (employee_id)
        REFERENCES employees(id),

    CONSTRAINT employee_time_off_valid_times
        CHECK (
            (
                full_day = TRUE
                AND start_time IS NULL
                AND end_time IS NULL
            )
            OR
            (
                full_day = FALSE
                AND start_time IS NOT NULL
                AND end_time IS NOT NULL
                AND start_time < end_time
            )
        )
);

CREATE INDEX employee_time_off_employee_date_idx
    ON employee_time_off (
        employee_id,
        time_off_date
    );

CREATE UNIQUE INDEX employee_time_off_full_day_unique_idx
    ON employee_time_off (
        employee_id,
        time_off_date
    )
    WHERE full_day = TRUE;
