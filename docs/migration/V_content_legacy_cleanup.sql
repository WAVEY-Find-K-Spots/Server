-- =============================================================================
-- WAVEY content legacy cleanup
-- Target: wavey_db (Docker postgres / local)
--
-- contents      : 엔티티(title_ko, title_en, category)에 맞게 레거시 컬럼 제거
-- spot_contents : 엔티티(spot_id, content_id)에 맞게 레거시 컬럼 제거
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. contents 레거시 컬럼 제거
-- -----------------------------------------------------------------------------
ALTER TABLE contents DROP CONSTRAINT IF EXISTS contents_platform_check;

ALTER TABLE contents DROP COLUMN IF EXISTS description;
ALTER TABLE contents DROP COLUMN IF EXISTS external_id;
ALTER TABLE contents DROP COLUMN IF EXISTS platform;
ALTER TABLE contents DROP COLUMN IF EXISTS thumbnail_url;
ALTER TABLE contents DROP COLUMN IF EXISTS title;

-- title_ko / category 가 비어 있으면 안 되므로 보강 (이미 NOT NULL이면 무해)
ALTER TABLE contents
    ALTER COLUMN title_ko SET NOT NULL,
    ALTER COLUMN category SET NOT NULL;

-- -----------------------------------------------------------------------------
-- 2. spot_contents 레거시 컬럼 제거
-- -----------------------------------------------------------------------------
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

COMMIT;

-- =============================================================================
-- 최종 스키마 요약
--
-- contents
--   content_id, title_ko, title_en, category, created_at, updated_at
--   UNIQUE (title_ko, category)
--
-- spot_contents
--   id, spot_id, content_id, created_at, updated_at
--   UNIQUE (spot_id, content_id)
-- =============================================================================
