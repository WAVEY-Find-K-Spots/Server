ALTER TABLE spot_contents DROP CONSTRAINT IF EXISTS spot_contents_kind_check;

ALTER TABLE spot_contents DROP COLUMN IF EXISTS artist;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS description_en;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS display_order;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS duration_seconds;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS episodes;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS kind;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS playback_url;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS scene_description;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS scene_description_en;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS scene_url;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS title_en;

ALTER TABLE spot_contents
    ALTER COLUMN spot_id SET NOT NULL,
    ALTER COLUMN content_id SET NOT NULL;

ALTER TABLE spot_contents DROP CONSTRAINT IF EXISTS uk_spot_contents_spot_content;

ALTER TABLE spot_contents
    ADD CONSTRAINT uk_spot_contents_spot_content UNIQUE (spot_id, content_id);
