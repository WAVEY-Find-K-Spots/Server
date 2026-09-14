ALTER TABLE regions
    ADD COLUMN IF NOT EXISTS name_ko VARCHAR(50),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(50);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'regions'
          AND column_name = 'name'
    ) THEN
        UPDATE regions
        SET name_ko = COALESCE(NULLIF(name_ko, ''), name)
        WHERE name_ko IS NULL;
    END IF;
END $$;

ALTER TABLE regions
    ALTER COLUMN name_ko SET NOT NULL;

DROP INDEX IF EXISTS idx_regions_code;

ALTER TABLE regions
    DROP COLUMN IF EXISTS code,
    DROP COLUMN IF EXISTS name,
    DROP COLUMN IF EXISTS latitude,
    DROP COLUMN IF EXISTS longitude;

CREATE INDEX IF NOT EXISTS idx_regions_name_ko
    ON regions (name_ko);
