-- 로컬 로그인 계정의 BCrypt 비밀번호 해시 저장용 컬럼입니다.
-- OAuth 계정은 비밀번호를 사용하지 않으므로 NULL을 허용합니다.
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);