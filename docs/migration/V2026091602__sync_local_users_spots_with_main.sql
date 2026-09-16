-- Local schema → match main (Railway) for columns the entity/OAuth already expect.
-- Additive + rename only. Does NOT drop local-only tables (works, work_*, etc.).

-- 1) users.language (OAuth 500: column does not exist)
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS language varchar(10) NOT NULL DEFAULT 'KO';

-- 2) user_settings.spot_enabled
ALTER TABLE user_settings
    ADD COLUMN IF NOT EXISTS spot_enabled boolean NOT NULL DEFAULT true;

-- 3) notifications.event_key
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS event_key varchar(160);

-- 4) spots: main/entity use name_ko / address_ko (local still has name / address)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'spots' AND column_name = 'name'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'spots' AND column_name = 'name_ko'
    ) THEN
        ALTER TABLE spots RENAME COLUMN name TO name_ko;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'spots' AND column_name = 'address'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'spots' AND column_name = 'address_ko'
    ) THEN
        ALTER TABLE spots RENAME COLUMN address TO address_ko;
    END IF;
END $$;

-- Align nullability with main after backfill-safe rename
ALTER TABLE spots
    ALTER COLUMN name_ko SET NOT NULL,
    ALTER COLUMN address_ko SET NOT NULL;
