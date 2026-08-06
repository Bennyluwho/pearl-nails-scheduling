CREATE TABLE service_categories (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT service_categories_name_not_blank
        CHECK (char_length(trim(name)) > 0),

    CONSTRAINT service_categories_display_order_nonnegative
        CHECK (display_order >= 0)
);

CREATE UNIQUE INDEX service_categories_unique_active_name
    ON service_categories (lower(name))
    WHERE active = TRUE;


CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,

    category_id BIGINT NOT NULL,

    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),

    price NUMERIC(10, 2) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    buffer_minutes INTEGER NOT NULL DEFAULT 0,

    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT services_category_fk
        FOREIGN KEY (category_id)
        REFERENCES service_categories(id),

    CONSTRAINT services_name_not_blank
        CHECK (char_length(trim(name)) > 0),

    CONSTRAINT services_price_nonnegative
        CHECK (price >= 0),

    CONSTRAINT services_duration_positive
        CHECK (duration_minutes > 0),

    CONSTRAINT services_buffer_nonnegative
        CHECK (buffer_minutes >= 0),

    CONSTRAINT services_display_order_nonnegative
        CHECK (display_order >= 0)
);

CREATE UNIQUE INDEX services_unique_active_name_per_category
    ON services (category_id, lower(name))
    WHERE active = TRUE;

CREATE INDEX services_category_id_index
    ON services (category_id);

CREATE INDEX services_active_index
    ON services (active);