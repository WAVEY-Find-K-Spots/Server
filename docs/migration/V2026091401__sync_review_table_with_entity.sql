-- 1. 기존 복수형 테이블명이 있으면 엔티티명과 맞춘다.
DO $$
BEGIN
    IF to_regclass('public.review') IS NULL
       AND to_regclass('public.reviews') IS NOT NULL THEN
        ALTER TABLE reviews RENAME TO review;
    END IF;
END $$;

-- 2. review 테이블 기본 구조를 생성한다.
CREATE TABLE IF NOT EXISTS review (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 3. 누락 컬럼을 보강한다.
ALTER TABLE review
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS spot_id BIGINT,
    ADD COLUMN IF NOT EXISTS rating DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS body VARCHAR(2000),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- 4. rating을 Double 엔티티에 맞춰 double precision으로 변경한다.
ALTER TABLE review
    ALTER COLUMN rating TYPE DOUBLE PRECISION
    USING rating::DOUBLE PRECISION;

-- 5. 기존 데이터의 필수값을 보정한다.
UPDATE review
SET rating = COALESCE(rating, 1.0),
    body = COALESCE(NULLIF(body, ''), '내용 없음'),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP)
WHERE rating IS NULL
   OR body IS NULL
   OR body = ''
   OR created_at IS NULL;

-- 6. 엔티티의 NOT NULL 조건을 반영한다.
ALTER TABLE review
    ALTER COLUMN user_id SET NOT NULL,
    ALTER COLUMN spot_id SET NOT NULL,
    ALTER COLUMN rating SET NOT NULL,
    ALTER COLUMN body SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL;

-- 7. 평점 범위를 1.0~5.0으로 제한한다.
ALTER TABLE review
    DROP CONSTRAINT IF EXISTS ck_review_rating_range,
    ADD CONSTRAINT ck_review_rating_range CHECK (rating >= 1.0 AND rating <= 5.0);

-- 8. 사용자별 장소 리뷰 중복을 막는다.
ALTER TABLE review
    DROP CONSTRAINT IF EXISTS uk_review_user_spot,
    ADD CONSTRAINT uk_review_user_spot UNIQUE (user_id, spot_id);

-- 9. 장소 리뷰 목록 조회용 인덱스를 생성한다.
CREATE INDEX IF NOT EXISTS idx_review_spot_created
    ON review (spot_id, created_at DESC, id DESC);

-- 10. 사용자 리뷰 목록 조회용 인덱스를 생성한다.
CREATE INDEX IF NOT EXISTS idx_review_user_created
    ON review (user_id, created_at DESC, id DESC);
