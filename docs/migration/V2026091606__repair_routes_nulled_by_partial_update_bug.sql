-- #140 버그(PATCH /routes/{routeId}가 부분 필드만 보내도 나머지 필드를 null로 덮어씀)로
-- description(전체 4건)과 name(1건)이 null이 된 기존 루트 데이터를 복구한다.
-- 원본 값은 복구 불가능하므로, name은 동일 유저의 다른 루트에서 쓰인 기본값("내 루트")으로,
-- description은 빈 문자열로 채워 null로 인한 표시 오류(NPE, "null" 문자열 노출 등)만 방지한다.

UPDATE routes SET description = '' WHERE id IN (2022, 2024, 2025, 2026) AND description IS NULL;

UPDATE routes SET name = '내 루트' WHERE id = 2026 AND name IS NULL;
