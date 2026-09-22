-- Match the image-first ordering expression used by the search query.
CREATE INDEX IF NOT EXISTS idx_spots_image_popular_order
    ON spots (
        (CASE WHEN image_url IS NOT NULL AND image_url <> '' THEN 0 ELSE 1 END),
        saved_count DESC,
        review_count DESC,
        spot_id DESC
    );

CREATE INDEX IF NOT EXISTS idx_spots_image_rating_order
    ON spots (
        (CASE WHEN image_url IS NOT NULL AND image_url <> '' THEN 0 ELSE 1 END),
        avg_rating DESC,
        review_count DESC,
        spot_id DESC
    );

CREATE INDEX IF NOT EXISTS idx_spots_image_latest_order
    ON spots (
        (CASE WHEN image_url IS NOT NULL AND image_url <> '' THEN 0 ELSE 1 END),
        created_at DESC,
        spot_id DESC
    );
