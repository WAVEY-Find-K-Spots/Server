-- Vxx__optimize_spot_query.sql

CREATE INDEX IF NOT EXISTS idx_spot_region_category
    ON spot (region_id, category);

CREATE INDEX IF NOT EXISTS idx_spot_place_type
    ON spot (place_type);

CREATE INDEX IF NOT EXISTS idx_spot_rating
    ON spot (avg_rating DESC, review_count DESC, spot_id DESC);

CREATE INDEX IF NOT EXISTS idx_spot_popular
    ON spot (saved_count DESC, review_count DESC, spot_id DESC);

CREATE INDEX IF NOT EXISTS idx_spot_latest
    ON spot (created_at DESC, spot_id DESC);

CREATE INDEX IF NOT EXISTS idx_spot_latitude
    ON spot (latitude);

CREATE INDEX IF NOT EXISTS idx_spot_longitude
    ON spot (longitude);

CREATE INDEX IF NOT EXISTS idx_spot_category_rating
    ON spot (category, avg_rating DESC);


-- keyword 검색 가속
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_spot_name_ko_trgm
    ON spot
    USING gin (name_ko gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_spot_name_en_trgm
    ON spot
    USING gin (name_en gin_trgm_ops);