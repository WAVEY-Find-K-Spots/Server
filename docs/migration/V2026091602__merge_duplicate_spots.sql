-- 스팟 중복 정리 (name_ko + address_ko + category 완전 일치 그룹 → 최소 spot_id로 병합)
--
-- 배경: SpotSyncMediaLocationClient가 외부 데이터(드라마/영화 촬영지)를 동기화할 때
-- dedup 로직(V2026091501, 2026-09-15 12:46 배포)이 생기기 전까지는 같은 물리적 장소가
-- 소스 데이터에 여러 행(드라마 회차별)으로 나올 때마다 매번 새 Spot을 insert했다.
-- 확인 결과 dedup 로직 배포 이후로는 신규 중복이 생기지 않음(코드 수정 불필요) —
-- 이 마이그레이션은 과거에 쌓인 기존 중복 데이터만 정리한다.
--
-- 처리 대상: 1,963개 그룹, 6,310개 초과 행 (2026-09-16 Railway 확인 기준)
--
-- ⚠️ Railway Query 탭에서 위→아래 순서로 실행. 실행 전 가능하면 DB 스냅샷/백업 권장.

-- 0. 그룹별 canonical spot_id 매핑 테이블 생성
DROP TABLE IF EXISTS spot_dedup_map;
CREATE TABLE spot_dedup_map AS
SELECT
    s.spot_id,
    min(s.spot_id) OVER (PARTITION BY s.name_ko, s.address_ko, s.category) AS canonical_id
FROM spots s;

-- 1. route_spots: 그냥 canonical로 재지정 (유니크 제약 없음)
UPDATE route_spots rs
SET spot_id = m.canonical_id
FROM spot_dedup_map m
WHERE rs.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 2. saved_spots: (user_id, canonical) 이미 있으면 loser 행 삭제 후 재지정
DELETE FROM saved_spots ss
USING spot_dedup_map m
WHERE ss.spot_id = m.spot_id AND m.spot_id <> m.canonical_id
  AND EXISTS (
      SELECT 1 FROM saved_spots ss2
      WHERE ss2.user_id = ss.user_id AND ss2.spot_id = m.canonical_id
  );

UPDATE saved_spots ss
SET spot_id = m.canonical_id
FROM spot_dedup_map m
WHERE ss.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 3. reviews: (user_id, canonical) 이미 있으면 loser 리뷰 삭제 후 재지정
DELETE FROM reviews r
USING spot_dedup_map m
WHERE r.spot_id = m.spot_id AND m.spot_id <> m.canonical_id
  AND EXISTS (
      SELECT 1 FROM reviews r2
      WHERE r2.user_id = r.user_id AND r2.spot_id = m.canonical_id
  );

UPDATE reviews r
SET spot_id = m.canonical_id
FROM spot_dedup_map m
WHERE r.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 4. spot_contents: (content_id, canonical) 이미 있으면 loser 링크 삭제 후 재지정
DELETE FROM spot_contents sc
USING spot_dedup_map m
WHERE sc.spot_id = m.spot_id AND m.spot_id <> m.canonical_id
  AND EXISTS (
      SELECT 1 FROM spot_contents sc2
      WHERE sc2.content_id = sc.content_id AND sc2.spot_id = m.canonical_id
  );

UPDATE spot_contents sc
SET spot_id = m.canonical_id
FROM spot_dedup_map m
WHERE sc.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 5. badge_spots: (badge_id, canonical) 이미 있으면 loser 링크 삭제 후 재지정
DELETE FROM badge_spots bs
USING spot_dedup_map m
WHERE bs.spot_id = m.spot_id AND m.spot_id <> m.canonical_id
  AND EXISTS (
      SELECT 1 FROM badge_spots bs2
      WHERE bs2.badge_id = bs.badge_id AND bs2.spot_id = m.canonical_id
  );

UPDATE badge_spots bs
SET spot_id = m.canonical_id
FROM spot_dedup_map m
WHERE bs.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 6. stamps/user_stamps: stamps.spot_id가 UNIQUE라 그룹 내 여러 stamps 행을 하나로 합쳐야 함
DROP TABLE IF EXISTS spot_dedup_stamp_map;
CREATE TABLE spot_dedup_stamp_map AS
SELECT
    st.id AS stamp_id,
    st.spot_id,
    m.canonical_id,
    min(st.id) OVER (PARTITION BY m.canonical_id) AS canonical_stamp_id
FROM stamps st
JOIN spot_dedup_map m ON st.spot_id = m.spot_id;

-- 6-1. canonical stamp 행의 spot_id를 canonical spot으로 재지정
UPDATE stamps
SET spot_id = sm.canonical_id
FROM spot_dedup_stamp_map sm
WHERE stamps.id = sm.canonical_stamp_id;

-- 6-2. user_stamps: (user_id, canonical_stamp) 이미 있으면 loser 행 삭제 후 재지정
DELETE FROM user_stamps us
USING spot_dedup_stamp_map sm
WHERE us.stamp_id = sm.stamp_id AND sm.stamp_id <> sm.canonical_stamp_id
  AND EXISTS (
      SELECT 1 FROM user_stamps us2
      WHERE us2.user_id = us.user_id AND us2.stamp_id = sm.canonical_stamp_id
  );

UPDATE user_stamps us
SET stamp_id = sm.canonical_stamp_id
FROM spot_dedup_stamp_map sm
WHERE us.stamp_id = sm.stamp_id AND sm.stamp_id <> sm.canonical_stamp_id;

-- 6-3. user_stamps의 비정규화된 spot_id 컬럼도 canonical로 정리
UPDATE user_stamps us
SET spot_id = m.canonical_id
FROM spot_dedup_map m
WHERE us.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 6-4. 이제 남은 loser stamps 행 삭제
DELETE FROM stamps st
USING spot_dedup_stamp_map sm
WHERE st.id = sm.stamp_id AND sm.stamp_id <> sm.canonical_stamp_id;

-- 7. loser 스팟 삭제
DELETE FROM spots s
USING spot_dedup_map m
WHERE s.spot_id = m.spot_id AND m.spot_id <> m.canonical_id;

-- 8. 병합된 canonical 스팟들의 saved_count/review_count/avg_rating 재계산
UPDATE spots s
SET
    saved_count = (SELECT count(*) FROM saved_spots ss WHERE ss.spot_id = s.spot_id),
    review_count = (SELECT count(*) FROM reviews r WHERE r.spot_id = s.spot_id),
    avg_rating = COALESCE((SELECT avg(rating) FROM reviews r WHERE r.spot_id = s.spot_id), 0)
WHERE s.spot_id IN (
    SELECT DISTINCT canonical_id FROM spot_dedup_map WHERE spot_id <> canonical_id
);

-- 9. 정리
DROP TABLE spot_dedup_map;
DROP TABLE spot_dedup_stamp_map;

-- 10. 검증: 남은 중복이 없어야 함 (0건이어야 정상)
SELECT name_ko, address_ko, category, count(*)
FROM spots
GROUP BY name_ko, address_ko, category
HAVING count(*) > 1;
