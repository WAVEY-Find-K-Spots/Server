BEGIN;

-- Region master data is remapped by code, not by the old numeric ordering.
ALTER TABLE regions ADD COLUMN IF NOT EXISTS name_ko VARCHAR(100);
UPDATE regions SET name_ko = COALESCE(name_ko, name) WHERE name IS NOT NULL;

CREATE TEMP TABLE region_target (code VARCHAR(50) PRIMARY KEY, region_id BIGINT, name_ko VARCHAR(50), name_en VARCHAR(50)) ON COMMIT DROP;
INSERT INTO region_target VALUES
 ('SEOUL',1,'서울','Seoul'),('BUSAN',2,'부산','Busan'),('DAEGU',3,'대구','Daegu'),
 ('INCHEON',4,'인천','Incheon'),('GWANGJU',5,'광주','Gwangju'),('DAEJEON',6,'대전','Daejeon'),
 ('ULSAN',7,'울산','Ulsan'),('SEJONG',8,'세종','Sejong'),('GYEONGGI',9,'경기','Gyeonggi'),
 ('GANGWON',10,'강원','Gangwon'),('CHUNGBUK',11,'충북','Chungbuk'),('CHUNGNAM',12,'충남','Chungnam'),
 ('JEONBUK',13,'전북','Jeonbuk'),('JEONNAM',14,'전남','Jeonnam'),('GYEONGBUK',15,'경북','Gyeongbuk'),
 ('GYEONGNAM',16,'경남','Gyeongnam'),('JEJU',17,'제주','Jeju');

UPDATE spots SET region_id = region_id + 1000;
UPDATE regions SET region_id = region_id + 1000;
UPDATE spots s SET region_id = t.region_id
FROM regions r JOIN region_target t ON
    r.code = t.code OR r.code LIKE t.code || '_%'
WHERE s.region_id = r.region_id;
UPDATE spots SET region_id = CASE
    WHEN address LIKE '서울%' THEN 1 WHEN address LIKE '부산%' THEN 2 WHEN address LIKE '대구%' THEN 3
    WHEN address LIKE '인천%' THEN 4 WHEN address LIKE '광주%' THEN 5 WHEN address LIKE '대전%' THEN 6
    WHEN address LIKE '울산%' THEN 7 WHEN address LIKE '세종%' THEN 8 WHEN address LIKE '경기%' THEN 9
    WHEN address LIKE '강원%' THEN 10 WHEN address LIKE '충북%' THEN 11 WHEN address LIKE '충남%' THEN 12
    WHEN address LIKE '전북%' THEN 13 WHEN address LIKE '전남%' THEN 14 WHEN address LIKE '경북%' THEN 15
    WHEN address LIKE '경남%' THEN 16 WHEN address LIKE '제주%' THEN 17 ELSE region_id END
WHERE region_id >= 1000;
UPDATE regions SET code = 'LEGACY_' || region_id WHERE region_id >= 1000;
ALTER TABLE regions DROP COLUMN IF EXISTS name;
ALTER TABLE regions DROP COLUMN IF EXISTS latitude;
ALTER TABLE regions DROP COLUMN IF EXISTS longitude;
INSERT INTO regions (region_id, code, name_ko, name_en, created_at, updated_at)
SELECT region_id, code, name_ko, name_en, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM region_target;
DELETE FROM regions WHERE region_id >= 1000;
SELECT setval(pg_get_serial_sequence('regions','region_id'), 17, true);

DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='name')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='name_ko') THEN
        ALTER TABLE spots RENAME COLUMN name TO name_ko;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='address')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='address_ko') THEN
        ALTER TABLE spots RENAME COLUMN address TO address_ko;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='description')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='description_ko') THEN
        ALTER TABLE spots RENAME COLUMN description TO description_ko;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='opening_hours')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='opening_hours_ko') THEN
        ALTER TABLE spots RENAME COLUMN opening_hours TO opening_hours_ko;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='closed_days')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='closed_days_ko') THEN
        ALTER TABLE spots RENAME COLUMN closed_days TO closed_days_ko;
    END IF;
END $$;

UPDATE spots SET image_url = COALESCE(image_url, thumbnail_url) WHERE thumbnail_url IS NOT NULL;
ALTER TABLE spots DROP COLUMN IF EXISTS thumbnail_url;
ALTER TABLE spots DROP COLUMN IF EXISTS title;
ALTER TABLE spots DROP COLUMN IF EXISTS media_type;
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='spots' AND column_name='playlist_url') THEN
        IF EXISTS (SELECT 1 FROM spots WHERE playlist_url IS NOT NULL AND btrim(playlist_url) <> '') THEN
            RAISE EXCEPTION 'spots.playlist_url contains data; migrate it before dropping';
        END IF;
        ALTER TABLE spots DROP COLUMN playlist_url;
    END IF;
END $$;

ALTER TABLE contents ADD COLUMN IF NOT EXISTS title_ko VARCHAR(255);
ALTER TABLE contents ADD COLUMN IF NOT EXISTS title_en VARCHAR(255);
ALTER TABLE contents ADD COLUMN IF NOT EXISTS category VARCHAR(20);
UPDATE contents SET title_ko = title, category = CASE platform WHEN 'YOUTUBE' THEN 'DRAMA' WHEN 'SPOTIFY' THEN 'ARTIST' END
WHERE title_ko IS NULL OR category IS NULL;
ALTER TABLE contents DROP COLUMN IF EXISTS title;
ALTER TABLE contents DROP COLUMN IF EXISTS description;
ALTER TABLE contents DROP COLUMN IF EXISTS platform;
ALTER TABLE contents DROP COLUMN IF EXISTS external_id;
ALTER TABLE contents DROP COLUMN IF EXISTS thumbnail_url;
DROP INDEX IF EXISTS uk_contents_title_type;

DO $$ BEGIN
    IF to_regclass('work_videos') IS NOT NULL AND to_regclass('content_videos') IS NULL THEN
        ALTER TABLE work_videos RENAME TO content_videos;
        ALTER TABLE content_videos RENAME COLUMN work_video_id TO content_video_id;
        ALTER TABLE content_videos RENAME COLUMN work_id TO content_id;
    END IF;
    IF to_regclass('work_tracks') IS NOT NULL AND to_regclass('content_tracks') IS NULL THEN
        ALTER TABLE work_tracks RENAME TO content_tracks;
        ALTER TABLE content_tracks RENAME COLUMN work_track_id TO content_track_id;
        ALTER TABLE content_tracks RENAME COLUMN work_id TO content_id;
        ALTER TABLE content_tracks DROP COLUMN IF EXISTS album_name;
    END IF;
END $$;

INSERT INTO spot_contents (spot_id, content_id, created_at, updated_at)
SELECT sw.spot_id, sw.work_id, sw.created_at, sw.updated_at
FROM spot_works sw
WHERE NOT EXISTS (SELECT 1 FROM spot_contents sc WHERE sc.spot_id = sw.spot_id AND sc.content_id = sw.work_id);
ALTER TABLE spot_contents ADD CONSTRAINT uk_spot_contents_spot_content UNIQUE (spot_id, content_id);
ALTER TABLE spot_contents DROP COLUMN IF EXISTS kind;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS artist;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS episodes;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS title_en;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS description_en;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS scene_description;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS scene_description_en;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS playback_url;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS scene_url;
ALTER TABLE spot_contents DROP COLUMN IF EXISTS duration_seconds;
CREATE UNIQUE INDEX IF NOT EXISTS uk_contents_title_category ON contents(title_ko, category);

DO $$ BEGIN
    IF to_regclass('works') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM works) THEN DROP TABLE works; END IF;
    IF to_regclass('spot_works') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM spot_works) THEN DROP TABLE spot_works; END IF;
END $$;

COMMIT;
