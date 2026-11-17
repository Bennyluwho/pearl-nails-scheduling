
-- Allows employee IDs to participate in GiST constraints.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,

    employee_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,

    customer_name VARCHAR(150) NOT NULL,
    customer_phone VARCHAR(30),
    customer_email VARCHAR(255),

    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reserved_until TIMESTAMP WITH TIME ZONE NOT NULL,

    service_name VARCHAR(150) NOT NULL,
    price_at_booking NUMERIC(10, 2) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    buffer_minutes INTEGER NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT appointments_employee_fk
        FOREIGN KEY (employee_id)
        REFERENCES employees(id),

    CONSTRAINT appointments_service_fk
        FOREIGN KEY (service_id)
        REFERENCES services(id),

    CONSTRAINT appointments_customer_name_valid
        CHECK (char_length(trim(customer_name)) > 0),

    CONSTRAINT appointments_contact_required
        CHECK (
            (customer_phone IS NOT NULL
                AND char_length(trim(customer_phone)) > 0)
            OR
            (customer_email IS NOT NULL
                AND char_length(trim(customer_email)) > 0)
        ),

    CONSTRAINT appointments_time_range_valid
        CHECK (starts_at < ends_at),

    CONSTRAINT appointments_buffer_valid
        CHECK (reserved_until >= ends_at),

    CONSTRAINT appointments_price_valid
        CHECK (price_at_booking >= 0),

    CONSTRAINT appointments_duration_valid
        CHECK (duration_minutes > 0),

    CONSTRAINT appointments_buffer_minutes_valid
        CHECK (buffer_minutes >= 0),

    CONSTRAINT appointments_status_valid
        CHECK (
            status IN (
                'CONFIRMED',
                'COMPLETED',
                'CANCELLED',
                'NO_SHOW'
            )
        ),

    -- Protect against simultaneous bookings.
    CONSTRAINT appointments_no_employee_overlap
        EXCLUDE USING gist (
            employee_id WITH =,
            tstzrange(
                starts_at,
                reserved_until,
                '[)'
            ) WITH &&
        )
        WHERE (status <> 'CANCELLED')
);

CREATE INDEX appointments_employee_start_idx
    ON appointments (employee_id, starts_at);

CREATE INDEX appointments_start_idx
    ON appointments (starts_at);

CREATE INDEX appointments_service_idx
    ON appointments (service_id);
