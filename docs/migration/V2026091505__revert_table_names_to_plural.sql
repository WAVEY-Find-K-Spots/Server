-- 테이블명 컨벤션을 복수형으로 통일 (regions/spots/reviews).
--
-- 배경: Region(#47/PR#52), Spot(#48/PR#54), Review(#49/PR#56) 작업 중
-- @Table(name = "...")이 제거되어 dev 자동배포 이후 앱이 단수형 테이블
-- (region/spot/review)에 실데이터를 계속 쌓아왔다. 기존 복수형 테이블
-- (regions/spots/reviews)은 그 이전 시점의 값버림(stale) 데이터다.
--
-- 2026-09-15 Railway 확인 결과:
--   spot   15134 rows, 최신 2026-09-15  (실데이터) / spots   15134 rows, 최신 2026-09-04 (stale)
--   review    10 rows, 최신 2026-09-14  (실데이터) / reviews    10 rows, 최신 2026-09-08 (stale)
--   region    19 rows                   (실데이터) / regions    17 rows                  (stale)
--
-- ⚠️ Railway 대시보드 Query 탭은 한 번에 하나의 statement만 실행 가능하므로
-- 아래 문장을 위에서부터 하나씩 순서대로 실행할 것 (DROP 먼저, RENAME은 그다음).

-- 1. 값버림 복수형 테이블 삭제
DROP TABLE spots;
DROP TABLE reviews;
DROP TABLE regions;

-- 2. 실데이터가 있는 단수형 테이블을 복수형으로 rename
ALTER TABLE spot RENAME TO spots;
ALTER TABLE review RENAME TO reviews;
ALTER TABLE region RENAME TO regions;
