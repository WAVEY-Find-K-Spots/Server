-- routes.travel_mode CHECK 제약조건이 ('WALKING','TRANSIT','DRIVING')로 걸려있어
-- 코드에서 쓰는 TransportMode enum 값('WALK','TRANSIT','CAR')과 불일치, 루트 생성이
-- routes_travel_mode_check 위반으로 실패함(23514). 제약조건을 코드 enum에 맞춘다.
--
-- ⚠️ Railway 대시보드 Query 탭에서 위→아래 순서로 한 줄씩 실행할 것.
-- 기존 데이터에 'WALKING'/'DRIVING' 값이 남아있으면 제약조건 교체 전에
-- 먼저 새 값으로 변환해야 한다(2026-09-15 Railway 확인: WALKING 4건, DRIVING 3건, TRANSIT 3건).

UPDATE routes SET travel_mode = 'WALK' WHERE travel_mode = 'WALKING';
UPDATE routes SET travel_mode = 'CAR' WHERE travel_mode = 'DRIVING';

ALTER TABLE routes DROP CONSTRAINT routes_travel_mode_check;

ALTER TABLE routes ADD CONSTRAINT routes_travel_mode_check
    CHECK (travel_mode IN ('WALK', 'TRANSIT', 'CAR'));
