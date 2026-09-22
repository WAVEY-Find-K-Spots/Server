CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_spots_name_ko_trgm
    ON spots USING gin (lower(name_ko) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_spots_name_en_trgm
    ON spots USING gin (lower(name_en) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_spots_region_category
    ON spots (region_id, category);

CREATE INDEX IF NOT EXISTS idx_spots_popular
    ON spots (saved_count DESC, review_count DESC, spot_id DESC);

CREATE INDEX IF NOT EXISTS idx_spots_location
    ON spots (latitude, longitude);

CREATE INDEX IF NOT EXISTS idx_spot_contents_spot_id
    ON spot_contents (spot_id);

CREATE INDEX IF NOT EXISTS idx_spot_contents_content_id
    ON spot_contents (content_id);

CREATE INDEX IF NOT EXISTS idx_contents_title_ko_trgm
    ON contents USING gin (lower(title_ko) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_contents_title_en_trgm
    ON contents USING gin (lower(title_en) gin_trgm_ops);
