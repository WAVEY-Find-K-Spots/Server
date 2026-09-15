-- routes.travel_mode CHECK 제약조건이 ('WALKING','TRANSIT','DRIVING')로 걸려있어
-- 코드에서 쓰는 TransportMode enum 값('WALK','TRANSIT','CAR')과 불일치, 루트 생성이
-- routes_travel_mode_check 위반으로 실패함(23514). 제약조건을 코드 enum에 맞춘다.
--
-- ⚠️ Railway 대시보드 Query 탭에서 위→아래 순서로 한 줄씩 실행할 것.
-- 기존 제약조건이 'WALK'/'CAR' 값을 거부하므로, 값을 바꾸기 전에
-- 제약조건부터 먼저 삭제해야 한다(2026-09-15 Railway 확인: WALKING 4건, DRIVING 3건, TRANSIT 3건).

-- 1. 제약조건 먼저 삭제
ALTER TABLE routes DROP CONSTRAINT routes_travel_mode_check;

-- 2. 기존 값 변환
UPDATE routes SET travel_mode = 'WALK' WHERE travel_mode = 'WALKING';
UPDATE routes SET travel_mode = 'CAR' WHERE travel_mode = 'DRIVING';

-- 3. 새 제약조건 추가
ALTER TABLE routes ADD CONSTRAINT routes_travel_mode_check
    CHECK (travel_mode IN ('WALK', 'TRANSIT', 'CAR'));
