INSERT INTO service_categories (
    name,
    description,
    display_order
)
VALUES
    ('Manicures', 'Hand and natural nail services', 10),
    ('Pedicures', 'Foot and toenail services', 20),
    ('Acrylics', 'Acrylic nail enhancements and maintenance', 30),
    ('Add-ons', 'Optional additions to another service', 40);


INSERT INTO services (
    category_id,
    name,
    description,
    price,
    duration_minutes,
    buffer_minutes,
    display_order
)
VALUES
    (
        (SELECT id FROM service_categories WHERE name = 'Manicures'),
        'Classic Manicure',
        'Basic nail shaping, cuticle care, and polish',
        25.00,
        30,
        5,
        10
    ),
    (
        (SELECT id FROM service_categories WHERE name = 'Manicures'),
        'Gel Manicure',
        'Manicure finished with gel polish',
        40.00,
        45,
        5,
        20
    ),
    (
        (SELECT id FROM service_categories WHERE name = 'Pedicures'),
        'Classic Pedicure',
        'Basic foot and toenail care',
        35.00,
        40,
        10,
        10
    );