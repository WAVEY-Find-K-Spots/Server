-- Convert spots.place_type to the uppercase values used by Spot.PlaceType.
-- The column remains VARCHAR so PostgreSQL and the Java EnumType.STRING mapping stay compatible.
BEGIN;

ALTER TABLE spots
    ALTER COLUMN place_type TYPE varchar(30)
    USING place_type::varchar;

UPDATE spots
SET place_type = CASE
    WHEN place_type IS NULL OR btrim(place_type) = '' THEN 'OTHER'
    ELSE upper(replace(replace(btrim(place_type), '-', '_'), ' ', '_'))
END;

COMMIT;
