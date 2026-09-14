DO $$
BEGIN
    IF to_regclass('public.spot') IS NULL
       AND to_regclass('public.spots') IS NOT NULL THEN
        ALTER TABLE spots RENAME TO spot;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS spot (
    spot_id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'id'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'spot_id'
    ) THEN
        ALTER TABLE spot RENAME COLUMN id TO spot_id;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'name'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'name_ko'
    ) THEN
        ALTER TABLE spot RENAME COLUMN name TO name_ko;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'address'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'address_ko'
    ) THEN
        ALTER TABLE spot RENAME COLUMN address TO address_ko;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'description'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'description_ko'
    ) THEN
        ALTER TABLE spot RENAME COLUMN description TO description_ko;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'closed_days'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'closed_days_ko'
    ) THEN
        ALTER TABLE spot RENAME COLUMN closed_days TO closed_days_ko;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'transport_info'
    )
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'spot'
          AND column_name = 'transport_info_ko'
    ) THEN
        ALTER TABLE spot RENAME COLUMN transport_info TO transport_info_ko;
    END IF;
END $$;

ALTER TABLE spot
    ADD COLUMN IF NOT EXISTS region_id BIGINT,
    ADD COLUMN IF NOT EXISTS name_ko VARCHAR(255),
    ADD COLUMN IF NOT EXISTS name_en VARCHAR(255),
    ADD COLUMN IF NOT EXISTS category VARCHAR(30),
    ADD COLUMN IF NOT EXISTS place_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS description_ko TEXT,
    ADD COLUMN IF NOT EXISTS description_en TEXT,
    ADD COLUMN IF NOT EXISTS opening_hours VARCHAR(500),
    ADD COLUMN IF NOT EXISTS break_time VARCHAR(100),
    ADD COLUMN IF NOT EXISTS closed_days_ko VARCHAR(255),
    ADD COLUMN IF NOT EXISTS closed_days_en VARCHAR(255),
    ADD COLUMN IF NOT EXISTS tel VARCHAR(50),
    ADD COLUMN IF NOT EXISTS address_ko VARCHAR(500),
    ADD COLUMN IF NOT EXISTS address_en VARCHAR(500),
    ADD COLUMN IF NOT EXISTS transport_info_ko VARCHAR(500),
    ADD COLUMN IF NOT EXISTS transport_info_en VARCHAR(500),
    ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 8),
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(11, 8),
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS avg_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS review_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS saved_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

UPDATE spot
SET name_ko = COALESCE(NULLIF(name_ko, ''), '이름 없음')
WHERE name_ko IS NULL OR name_ko = '';

UPDATE spot
SET address_ko = COALESCE(NULLIF(address_ko, ''), '주소 없음')
WHERE address_ko IS NULL OR address_ko = '';

UPDATE spot
SET category = COALESCE(NULLIF(category, ''), 'K_HERITAGE')
WHERE category IS NULL OR category = '';

UPDATE spot
SET place_type = COALESCE(NULLIF(place_type, ''), 'OTHER')
WHERE place_type IS NULL OR place_type = '';

UPDATE spot
SET avg_rating = COALESCE(avg_rating, 0.0),
    review_count = COALESCE(review_count, 0),
    saved_count = COALESCE(saved_count, 0),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP)
WHERE avg_rating IS NULL
   OR review_count IS NULL
   OR saved_count IS NULL
   OR created_at IS NULL;

ALTER TABLE spot
    ALTER COLUMN region_id SET NOT NULL,
    ALTER COLUMN name_ko SET NOT NULL,
    ALTER COLUMN category SET NOT NULL,
    ALTER COLUMN place_type SET NOT NULL,
    ALTER COLUMN address_ko SET NOT NULL,
    ALTER COLUMN latitude SET NOT NULL,
    ALTER COLUMN longitude SET NOT NULL,
    ALTER COLUMN avg_rating SET NOT NULL,
    ALTER COLUMN review_count SET NOT NULL,
    ALTER COLUMN saved_count SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE spot
    ALTER COLUMN name_ko TYPE VARCHAR(255),
    ALTER COLUMN name_en TYPE VARCHAR(255),
    ALTER COLUMN category TYPE VARCHAR(30),
    ALTER COLUMN place_type TYPE VARCHAR(50),
    ALTER COLUMN opening_hours TYPE VARCHAR(500),
    ALTER COLUMN break_time TYPE VARCHAR(100),
    ALTER COLUMN closed_days_ko TYPE VARCHAR(255),
    ALTER COLUMN closed_days_en TYPE VARCHAR(255),
    ALTER COLUMN tel TYPE VARCHAR(50),
    ALTER COLUMN address_ko TYPE VARCHAR(500),
    ALTER COLUMN address_en TYPE VARCHAR(500),
    ALTER COLUMN transport_info_ko TYPE VARCHAR(500),
    ALTER COLUMN transport_info_en TYPE VARCHAR(500),
    ALTER COLUMN latitude TYPE DECIMAL(10, 8),
    ALTER COLUMN longitude TYPE DECIMAL(11, 8),
    ALTER COLUMN image_url TYPE VARCHAR(500);

DROP INDEX IF EXISTS idx_spots_region_id;
DROP INDEX IF EXISTS idx_spots_category;
DROP INDEX IF EXISTS idx_spots_location;
DROP INDEX IF EXISTS idx_spots_source_external;

ALTER TABLE spot
    DROP COLUMN IF EXISTS media_type,
    DROP COLUMN IF EXISTS title,
    DROP COLUMN IF EXISTS source_type,
    DROP COLUMN IF EXISTS external_content_id,
    DROP COLUMN IF EXISTS source_updated_at,
    DROP COLUMN IF EXISTS opening_hours_en;

CREATE INDEX IF NOT EXISTS idx_spot_region_id
    ON spot (region_id);

CREATE INDEX IF NOT EXISTS idx_spot_category
    ON spot (category);

CREATE INDEX IF NOT EXISTS idx_spot_place_type
    ON spot (place_type);

CREATE INDEX IF NOT EXISTS idx_spot_location
    ON spot (latitude, longitude);

CREATE INDEX IF NOT EXISTS idx_spot_region_category
    ON spot (region_id, category);

CREATE INDEX IF NOT EXISTS idx_spot_rating
    ON spot (avg_rating DESC, review_count DESC, spot_id DESC);

CREATE INDEX IF NOT EXISTS idx_spot_popular
    ON spot (saved_count DESC, review_count DESC, spot_id DESC);

CREATE INDEX IF NOT EXISTS idx_spot_latest
    ON spot (created_at DESC, spot_id DESC);

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_spot_name_ko_trgm
    ON spot USING gin (name_ko gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_spot_name_en_trgm
    ON spot USING gin (name_en gin_trgm_ops);
