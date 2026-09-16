-- Local mock media for content API testing.
-- Targets contents already linked to spots but missing videos/albums/tracks:
--   MOVIE 4,5,6 / ARTIST 7,8,9
-- Also wires demo spot_contents for category filter checks.
-- Idempotent: ON CONFLICT / NOT EXISTS.

-- ========== videos ==========
INSERT INTO content_videos (
    content_id, youtube_video_id, title, channel_title, thumbnail_url,
    duration_sec, fetched_at, hidden, created_at, updated_at
)
SELECT v.content_id, v.youtube_video_id, v.title, v.channel_title, v.thumbnail_url,
       v.duration_sec, now(), false, now(), now()
FROM (VALUES
    (4::bigint, 'isOGD_7hNIY', '기생충 공식 예고편', 'CJ ENM',
     'https://i.ytimg.com/vi/isOGD_7hNIY/hqdefault.jpg', 150),
    (4, '5xH0HfJHsaI', '기생충 티저', 'CJ ENM',
     'https://i.ytimg.com/vi/5xH0HfJHsaI/hqdefault.jpg', 120),
    (5, 'tAaM0dDFlFQ', '올드보이 예고편', 'CJ Entertainment',
     'https://i.ytimg.com/vi/tAaM0dDFlFQ/hqdefault.jpg', 140),
    (6, 'pyWuHv2-CWY', '부산행 예고편', 'Next Entertainment World',
     'https://i.ytimg.com/vi/pyWuHv2-CWY/hqdefault.jpg', 130),
    (7, 'D1PvIWdO8ss', 'IU - Blueming MV', '1theK',
     'https://i.ytimg.com/vi/D1PvIWdO8ss/hqdefault.jpg', 217),
    (7, '0-VAnh7r-_8', 'IU - Celebrity MV', 'edam',
     'https://i.ytimg.com/vi/0-VAnh7r-_8/hqdefault.jpg', 195),
    (8, 'gdZLi9oWNZg', 'BTS - Dynamite Official MV', 'HYBE LABELS',
     'https://i.ytimg.com/vi/gdZLi9oWNZg/hqdefault.jpg', 223),
    (8, 'kXpOEzNY_u0', 'BTS - Butter Official MV', 'HYBE LABELS',
     'https://i.ytimg.com/vi/kXpOEzNY_u0/hqdefault.jpg', 164),
    (9, 'js1CtxSY38I', 'NewJeans - Attention Official MV', 'HYBE LABELS',
     'https://i.ytimg.com/vi/js1CtxSY38I/hqdefault.jpg', 180),
    (9, 'p2cexL5B0dE', 'NewJeans - Hype Boy Official MV', 'HYBE LABELS',
     'https://i.ytimg.com/vi/p2cexL5B0dE/hqdefault.jpg', 179)
) AS v(content_id, youtube_video_id, title, channel_title, thumbnail_url, duration_sec)
WHERE EXISTS (SELECT 1 FROM contents c WHERE c.content_id = v.content_id)
ON CONFLICT ON CONSTRAINT uk_content_videos_content_youtube DO NOTHING;

-- ========== albums ==========
INSERT INTO content_albums (
    content_id, spotify_album_id, title, image_url, spotify_url,
    fetched_at, hidden, created_at, updated_at
)
SELECT a.content_id, a.spotify_album_id, a.title, a.image_url, a.spotify_url,
       now(), false, now(), now()
FROM (VALUES
    (4::bigint, 'mock_album_parasite', 'Parasite Original Soundtrack',
     'https://i.scdn.co/image/ab67616d0000b2731b94ec46427089df321e1298',
     'https://open.spotify.com/album/mock_album_parasite'),
    (5, 'mock_album_oldboy', 'Oldboy Original Soundtrack',
     'https://i.scdn.co/image/ab67616d0000b273782f5a82feaae8c270a10b29',
     'https://open.spotify.com/album/mock_album_oldboy'),
    (6, 'mock_album_traintobusan', 'Train to Busan Original Soundtrack',
     'https://i.scdn.co/image/ab67616d0000b2731b94ec46427089df321e1298',
     'https://open.spotify.com/album/mock_album_traintobusan'),
    (7, 'mock_album_iu_palette', 'Palette',
     'https://i.scdn.co/image/ab67616d0000b273782f5a82feaae8c270a10b29',
     'https://open.spotify.com/album/mock_album_iu_palette'),
    (8, 'mock_album_bts_be', 'BE',
     'https://i.scdn.co/image/ab67616d0000b2731b94ec46427089df321e1298',
     'https://open.spotify.com/album/mock_album_bts_be'),
    (9, 'mock_album_newjeans', 'NewJeans 1st EP ''New Jeans''',
     'https://i.scdn.co/image/ab67616d0000b273782f5a82feaae8c270a10b29',
     'https://open.spotify.com/album/mock_album_newjeans')
) AS a(content_id, spotify_album_id, title, image_url, spotify_url)
WHERE EXISTS (SELECT 1 FROM contents c WHERE c.content_id = a.content_id)
ON CONFLICT ON CONSTRAINT uk_content_albums_content_spotify DO NOTHING;

-- ========== album tracks ==========
INSERT INTO content_tracks (
    content_id, content_album_id, spotify_track_id, title, artist_name,
    image_url, spotify_url, duration_ms, fetched_at, hidden, created_at, updated_at
)
SELECT t.content_id, al.content_album_id, t.spotify_track_id, t.title, t.artist_name,
       al.image_url, t.spotify_url, t.duration_ms, now(), false, now(), now()
FROM (VALUES
    (4::bigint, 'mock_album_parasite', 'mock_tr_parasite_1', 'Belt of Faith', 'Jung Jaeil',
     'https://open.spotify.com/track/mock_tr_parasite_1', 180000::bigint),
    (4, 'mock_album_parasite', 'mock_tr_parasite_2', 'Dining Together', 'Jung Jaeil',
     'https://open.spotify.com/track/mock_tr_parasite_2', 210000),
    (5, 'mock_album_oldboy', 'mock_tr_oldboy_1', 'The Last Waltz', 'Cho Young-Wuk',
     'https://open.spotify.com/track/mock_tr_oldboy_1', 200000),
    (6, 'mock_album_traintobusan', 'mock_tr_ttb_1', 'Good Morning', 'Jang Young-gyu',
     'https://open.spotify.com/track/mock_tr_ttb_1', 195000),
    (7, 'mock_album_iu_palette', 'mock_tr_iu_1', 'Palette (feat. G-DRAGON)', 'IU',
     'https://open.spotify.com/track/mock_tr_iu_1', 217000),
    (7, 'mock_album_iu_palette', 'mock_tr_iu_2', 'Through the Night', 'IU',
     'https://open.spotify.com/track/mock_tr_iu_2', 253000),
    (8, 'mock_album_bts_be', 'mock_tr_bts_1', 'Life Goes On', 'BTS',
     'https://open.spotify.com/track/mock_tr_bts_1', 207000),
    (8, 'mock_album_bts_be', 'mock_tr_bts_2', 'Dynamite', 'BTS',
     'https://open.spotify.com/track/mock_tr_bts_2', 199000),
    (9, 'mock_album_newjeans', 'mock_tr_nj_1', 'Attention', 'NewJeans',
     'https://open.spotify.com/track/mock_tr_nj_1', 180000),
    (9, 'mock_album_newjeans', 'mock_tr_nj_2', 'Hype Boy', 'NewJeans',
     'https://open.spotify.com/track/mock_tr_nj_2', 179000)
) AS t(content_id, spotify_album_id, spotify_track_id, title, artist_name, spotify_url, duration_ms)
JOIN content_albums al
  ON al.content_id = t.content_id AND al.spotify_album_id = t.spotify_album_id
ON CONFLICT ON CONSTRAINT uk_content_tracks_content_spotify_track DO NOTHING;

-- ========== standalone tracks (album_id NULL) ==========
INSERT INTO content_tracks (
    content_id, content_album_id, spotify_track_id, title, artist_name,
    image_url, spotify_url, duration_ms, fetched_at, hidden, created_at, updated_at
)
SELECT t.content_id, NULL, t.spotify_track_id, t.title, t.artist_name,
       t.image_url, t.spotify_url, t.duration_ms, now(), false, now(), now()
FROM (VALUES
    (7::bigint, 'mock_tr_iu_solo_1', 'Good Day', 'IU',
     'https://i.scdn.co/image/ab67616d0000b273782f5a82feaae8c270a10b29',
     'https://open.spotify.com/track/mock_tr_iu_solo_1', 233000::bigint),
    (8, 'mock_tr_bts_solo_1', 'Spring Day', 'BTS',
     'https://i.scdn.co/image/ab67616d0000b2731b94ec46427089df321e1298',
     'https://open.spotify.com/track/mock_tr_bts_solo_1', 255000),
    (9, 'mock_tr_nj_solo_1', 'Ditto', 'NewJeans',
     'https://i.scdn.co/image/ab67616d0000b273782f5a82feaae8c270a10b29',
     'https://open.spotify.com/track/mock_tr_nj_solo_1', 185000),
    (4, 'mock_tr_parasite_solo', 'Ending', 'Jung Jaeil',
     'https://i.scdn.co/image/ab67616d0000b2731b94ec46427089df321e1298',
     'https://open.spotify.com/track/mock_tr_parasite_solo', 160000)
) AS t(content_id, spotify_track_id, title, artist_name, image_url, spotify_url, duration_ms)
WHERE EXISTS (SELECT 1 FROM contents c WHERE c.content_id = t.content_id)
ON CONFLICT ON CONSTRAINT uk_content_tracks_content_spotify_track DO NOTHING;

-- ========== demo spot_contents ==========
-- spot 1: DRAMA+MOVIE+ARTIST (full mix)
-- spot 2: DRAMA only
-- spot 3: ARTIST only
DELETE FROM spot_contents WHERE spot_id = 1 AND content_id NOT IN (1,2,3,4,5,6,7,8,9);
DELETE FROM spot_contents WHERE spot_id = 2 AND content_id NOT IN (1,2,3);
DELETE FROM spot_contents WHERE spot_id = 3 AND content_id NOT IN (7,8,9);

INSERT INTO spot_contents (spot_id, content_id, created_at, updated_at)
SELECT s.spot_id, s.content_id, now(), now()
FROM (VALUES
    (1::bigint, 1::bigint),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 9),
    (2, 1),
    (2, 2),
    (2, 3),
    (3, 7),
    (3, 8),
    (3, 9)
) AS s(spot_id, content_id)
WHERE EXISTS (SELECT 1 FROM spots sp WHERE sp.spot_id = s.spot_id)
  AND EXISTS (SELECT 1 FROM contents c WHERE c.content_id = s.content_id)
ON CONFLICT ON CONSTRAINT uk_spot_contents_spot_content DO NOTHING;
