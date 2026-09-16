-- contents.category: HERITAGE 허용 (앱 enum ContentCategory)
-- 로컬 DB에 contents_category_check 가 있으면 HERITAGE 추가.

ALTER TABLE contents DROP CONSTRAINT IF EXISTS contents_category_check;

ALTER TABLE contents
    ADD CONSTRAINT contents_category_check
        CHECK (category IN ('ARTIST', 'DRAMA', 'MOVIE', 'HERITAGE'));

COMMENT ON COLUMN contents.category IS 'ARTIST | DRAMA | MOVIE | HERITAGE';
