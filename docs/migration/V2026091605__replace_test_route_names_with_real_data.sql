-- 공개/비공개 루트(id 2001~2010)에 들어있던 "Swagger Route N" 플레이스홀더 이름/설명을
-- 연결된 실제 스팟(route_spots) 기준의 실데이터로 교체한다.
-- route_spots 연결(spot_id)과 visibility/travel_mode는 이미 실제 데이터이므로 그대로 유지한다.

UPDATE routes SET name = '충남 해송과바다 드라마 촬영지 코스',
    description = '충남 해송과바다에서 즐기는 K-드라마 촬영지 산책 코스', updated_at = NOW()
WHERE id = 2001;

UPDATE routes SET name = '경기 안성팜랜드 K-POP 코스',
    description = '경기 안성팜랜드를 둘러보는 K-POP 테마 대중교통 코스', updated_at = NOW()
WHERE id = 2002;

UPDATE routes SET name = '서울 엘빈에비뉴라운지 K-POP 코스',
    description = '서울 엘빈에비뉴라운지에서 즐기는 K-POP 감성 드라이브 코스', updated_at = NOW()
WHERE id = 2003;

UPDATE routes SET name = '부산 참참참 K-POP 코스',
    description = '부산 참참참을 둘러보는 K-POP 테마 도보 코스', updated_at = NOW()
WHERE id = 2004;

UPDATE routes SET name = '서울 명품먹태&노가리 K-POP 코스',
    description = '서울 명품먹태&노가리에서 즐기는 K-POP 테마 대중교통 코스', updated_at = NOW()
WHERE id = 2005;

UPDATE routes SET name = '서울 니가사니 K-POP 코스',
    description = '서울 니가사니를 둘러보는 K-POP 테마 드라이브 코스', updated_at = NOW()
WHERE id = 2006;

UPDATE routes SET name = '경기 아소산 일산본점 드라마 촬영지 코스',
    description = '경기 아소산 일산본점에서 즐기는 K-드라마 촬영지 도보 코스', updated_at = NOW()
WHERE id = 2007;

UPDATE routes SET name = '경기 네오위즈 드라마 촬영지 코스',
    description = '경기 네오위즈 일대 K-드라마 촬영지를 둘러보는 대중교통 코스', updated_at = NOW()
WHERE id = 2008;

UPDATE routes SET name = '경기 퍼스트가든 드라마 촬영지 코스',
    description = '경기 퍼스트가든에서 즐기는 K-드라마 촬영지 드라이브 코스', updated_at = NOW()
WHERE id = 2009;

UPDATE routes SET name = '서울 코엑스 드라마 촬영지 코스',
    description = '서울 코엑스 일대 K-드라마 촬영지를 둘러보는 도보 코스', updated_at = NOW()
WHERE id = 2010;
