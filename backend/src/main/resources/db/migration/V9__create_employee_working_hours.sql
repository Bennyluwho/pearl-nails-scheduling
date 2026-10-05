CREATE TABLE employee_working_hours (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    day_of_week SMALLINT NOT NULL,

    start_time TIME WITHOUT TIME ZONE NOT NULL,
    end_time TIME WITHOUT TIME ZONE NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT employee_working_hours_employee_fk
        FOREIGN KEY (employee_id)
        REFERENCES employees(id),

    CONSTRAINT employee_working_hours_day_range
        CHECK (day_of_week BETWEEN 1 AND 7),

    CONSTRAINT employee_working_hours_valid_time_range
        CHECK (start_time < end_time),

    CONSTRAINT employee_working_hours_unique_shift
        UNIQUE (
            employee_id,
            day_of_week,
            start_time,
            end_time
        )
);

CREATE INDEX employee_working_hours_employee_index
    ON employee_working_hours(employee_id);

CREATE INDEX employee_working_hours_employee_day_index
    ON employee_working_hours(
        employee_id,
        day_of_week
    );