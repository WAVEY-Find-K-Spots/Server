-- routes.travel_mode는 항상 기본값(WALK)으로만 저장되고 어디에서도 읽히지 않는 컬럼이다.
-- 길찾기 이동수단은 POST /routes/{routeId}/directions 요청의 transportMode로 매번 받으므로 컬럼을 제거한다.
-- 엔티티에서 필드를 제거한 뒤에는 INSERT에 travel_mode가 포함되지 않아 NOT NULL 제약에 걸리므로,
-- 애플리케이션 배포 전에 먼저 적용해야 한다.

ALTER TABLE routes DROP COLUMN IF EXISTS travel_mode;
