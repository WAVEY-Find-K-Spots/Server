-- Swagger 테스트로 들어간 가짜 배지(id 2001~2010, 존재하지 않는 region_id 참조)를 삭제하고
-- 실제 지역/카테고리 스팟 분포 기준의 배지 8개로 교체한다.
--
-- ⚠️ Railway Query 탭에서 위→아래 순서로 실행.

-- 1. 기존 테스트 배지 및 연관 데이터 삭제
DELETE FROM user_badges WHERE badge_id BETWEEN 2001 AND 2010;
DELETE FROM badge_spots WHERE badge_id BETWEEN 2001 AND 2010;
DELETE FROM badges WHERE id BETWEEN 2001 AND 2010;

-- 2. 실제 데이터 기반 배지 8개 생성 (id는 자동 생성에 맡김)
INSERT INTO badges (name, name_en, description, description_en, required_stamps, region_id, category, created_at, updated_at)
VALUES
    ('서울 탐험가', 'Seoul Explorer', '서울 스팟 10곳 방문', 'Visit 10 spots in Seoul', 10, 1, NULL, now(), now()),
    ('경기 러버', 'Gyeonggi Lover', '경기 스팟 10곳 방문', 'Visit 10 spots in Gyeonggi', 10, 9, NULL, now(), now()),
    ('강원 여행자', 'Gangwon Traveler', '강원 스팟 5곳 방문', 'Visit 5 spots in Gangwon', 5, 10, NULL, now(), now()),
    ('제주 탐방가', 'Jeju Voyager', '제주 스팟 5곳 방문', 'Visit 5 spots in Jeju', 5, 17, NULL, now(), now()),
    ('K-POP 성지순례', 'K-POP Pilgrim', 'K-POP 스팟 20곳 방문', 'Visit 20 K-POP spots', 20, NULL, 'K_POP', now(), now()),
    ('K-드라마 덕후', 'K-Drama Fan', 'K-드라마 스팟 15곳 방문', 'Visit 15 K-Drama spots', 15, NULL, 'K_DRAMA', now(), now()),
    ('문화유산 지킴이', 'Heritage Keeper', '문화유산 스팟 10곳 방문', 'Visit 10 heritage spots', 10, NULL, 'K_HERITAGE', now(), now()),
    ('K-무비 마니아', 'K-Movie Maniac', 'K-영화 스팟 5곳 방문', 'Visit 5 K-Movie spots', 5, NULL, 'K_MOVIE', now(), now());

-- 3. 확인
SELECT id, name, name_en, description, required_stamps, region_id, category FROM badges ORDER BY id;
