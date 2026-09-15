SELECT setval(
    pg_get_serial_sequence('region', 'id'),
    COALESCE((SELECT MAX(id) FROM region), 0) + 1,
    false
);
