ALTER TABLE contents DROP CONSTRAINT IF EXISTS contents_platform_check;

ALTER TABLE contents DROP COLUMN IF EXISTS description;
ALTER TABLE contents DROP COLUMN IF EXISTS external_id;
ALTER TABLE contents DROP COLUMN IF EXISTS platform;
ALTER TABLE contents DROP COLUMN IF EXISTS thumbnail_url;
ALTER TABLE contents DROP COLUMN IF EXISTS title;

ALTER TABLE contents
    ALTER COLUMN title_ko SET NOT NULL,
    ALTER COLUMN category SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_contents_title_category
    ON contents (title_ko, category);
