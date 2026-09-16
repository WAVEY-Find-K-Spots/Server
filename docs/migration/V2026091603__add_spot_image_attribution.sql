-- Wikimedia Commons 등 저작자 표시가 필요한 이미지 소스를 위한 컬럼 추가.
-- CC BY/BY-SA 라이선스는 AttributionRequired인 경우가 많아, 프론트에서 이미지와 함께
-- 저작자/라이선스 텍스트를 노출할 수 있도록 별도 컬럼에 저장한다.

ALTER TABLE spots ADD COLUMN IF NOT EXISTS image_attribution VARCHAR(500);
