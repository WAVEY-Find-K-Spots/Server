# API 명세서

Wavey 서버의 도메인별 API 명세서 모음입니다. 모든 문서는 **현재 서버 구현 형태**를 기준으로 작성되며,
공통 규격(응답 봉투, 인증)은 [routes.md](./routes.md) 를 기준 문서로 삼습니다.

| 도메인 | 문서 | Base path | 요약 |
|--------|------|-----------|------|
| Auth | [auth.md](./auth.md) | `/api/v1/auth` | 소셜 로그인(OAuth2), JWT 발급/재발급(RTR), 회원 관리 |
| Route | [routes.md](./routes.md) | `/api/v1/routes` | 루트 CRUD, 루트 스팟 관리, 경로 계산(Directions), 루트 탭 매핑 |
| Spot | [spot.md](./spot.md) | `/api/v1/spots` | 장소 CRUD, 지도 범위 조회, 외부 공공데이터 동기화 |
| Content | [content.md](./content.md) | `/api/v1/contents` | 유튜브/스포티파이 콘텐츠 CRUD·검색 |
| Region | [region.md](./region.md) | `/api/v1/regions` | 지역 CRUD |

## 공통 규격 요약

- **응답 봉투**: `{ "statusCode", "message", "data" }` — 성공 시 `error` 키 생략, 실패 시 `data`/`message` 생략하고 `error: { code, message }`
- **인증**: `Authorization: Bearer {accessToken}` (JWT HS256). 토큰 오류 `401 TOKEN_401`, 만료 `401 TOKEN_402`, 권한 부족 `403 AUTH_403`
- **비인증 허용 경로** (`SecurityConfig` permitAll): `/api/v1/auth/login-urls`, `/api/v1/auth/refresh`, `/api/v1/routes/public`, `/api/v1/spots/sync/**`, Swagger, `/h2-console/**`
- **좌표**: WGS84, `latitude`/`longitude` (소수점 8자리)
- 에러 코드 상수 정의는 `global/exception/ErrorCode.java`
