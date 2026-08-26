CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(150),

    phone VARCHAR(30),
    email VARCHAR(225),

    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT employees_first_name_not_blank
        CHECK (char_length(trim(first_name)) > 0),
    
    CONSTRAINT employees_last_name_not_blank
        CHECK (char_length(trim(last_name)) > 0),

    CONSTRAINT employees_display_order_nonnegative
        CHECK (display_order >= 0)
        
);

CREATE INDEX employees_active_index
    ON employees (active);

CREATE INDEX employees_display_order_index
    ON employees (display_order);
