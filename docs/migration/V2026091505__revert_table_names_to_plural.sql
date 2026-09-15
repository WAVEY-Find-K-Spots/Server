-- 테이블명 컨벤션을 복수형으로 통일 (regions/spots/reviews).
-- V2026091302, V2026091401에서 단수형으로 rename했던 것을 되돌린다.
-- 컬럼/인덱스/제약조건은 이미 해당 마이그레이션들에서 정리되었으므로 테이블명만 변경한다.
--
-- ⚠️ Railway 대시보드 Query 탭은 한 번에 하나의 statement만 실행 가능하고
-- DO $$ ... $$ 같은 PL/pgSQL 익명 블록을 지원하지 않을 수 있으므로,
-- 아래 0단계로 먼저 현재 상태를 확인한 뒤, 해당하는 단계만 한 줄씩 실행할 것.

-- 0. 현재 상태 확인 (실행 결과에 따라 1~3단계 중 필요한 것만 실행)
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('spot', 'spots', 'review', 'reviews', 'region', 'regions');

-- 1. spot -> spots (결과에 'spots'가 없고 'spot'만 있을 때만 실행)
ALTER TABLE spot RENAME TO spots;

-- 2. review -> reviews (결과에 'reviews'가 없고 'review'만 있을 때만 실행)
ALTER TABLE review RENAME TO reviews;

-- 3. region 정리 (실제 데이터는 이미 regions에 있음)
-- 3-a. 'regions'와 'region'이 둘 다 있으면 빈 중복 테이블(region)만 삭제
DROP TABLE region;

-- 3-b. 'regions'는 없고 'region'만 있는 경우에는 3-a 대신 아래를 실행
-- ALTER TABLE region RENAME TO regions;
