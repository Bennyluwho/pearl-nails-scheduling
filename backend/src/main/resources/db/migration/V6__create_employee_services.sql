CREATE TABLE employee_services (
    employee_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT employee_services_pk
        PRIMARY KEY (employee_id, service_id),

    CONSTRAINT employee_services_employee_fk
        FOREIGN KEY (employee_id)
        REFERENCES employees(id),

    CONSTRAINT employee_services_service_fk
        FOREIGN KEY (service_id)
        REFERENCES services(id)
);

CREATE INDEX employee_services_service_id_index
    ON employee_services(service_id);