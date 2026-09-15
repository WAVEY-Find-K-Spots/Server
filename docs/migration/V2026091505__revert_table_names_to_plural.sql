-- 테이블명 컨벤션을 복수형으로 통일 (regions/spots/reviews).
-- V2026091302, V2026091401에서 단수형으로 rename했던 것을 되돌린다.
-- 컬럼/인덱스/제약조건은 이미 해당 마이그레이션들에서 정리되었으므로 테이블명만 변경한다.

-- spot -> spots
DO $$
BEGIN
    IF to_regclass('public.spots') IS NULL
       AND to_regclass('public.spot') IS NOT NULL THEN
        ALTER TABLE spot RENAME TO spots;
    END IF;
END $$;

-- review -> reviews
DO $$
BEGIN
    IF to_regclass('public.reviews') IS NULL
       AND to_regclass('public.review') IS NOT NULL THEN
        ALTER TABLE review RENAME TO reviews;
    END IF;
END $$;

-- region: 실제 데이터는 이미 regions에 있고, ddl-auto로 생성된 빈 region 테이블만 정리한다.
DO $$
BEGIN
    IF to_regclass('public.regions') IS NOT NULL
       AND to_regclass('public.region') IS NOT NULL THEN
        DROP TABLE region;
    ELSIF to_regclass('public.regions') IS NULL
          AND to_regclass('public.region') IS NOT NULL THEN
        ALTER TABLE region RENAME TO regions;
    END IF;
END $$;
