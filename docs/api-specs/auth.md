# Auth API 명세서

소셜 로그인(OAuth2) 및 회원 관리 도메인의 API 규격입니다.
응답 예시는 **현재 서버 구현 형태**를 기준으로 작성했습니다.

## 구현 상태

| 범위 | 상태 |
|------|------|
| 구글 소셜 로그인 (OAuth2 Authorization Code) | 구현됨 |
| JWT Access / Refresh 발급 + Refresh Token Rotation | 구현됨 |
| 회원 조회 / 권한 변경 / 탈퇴 / 로그아웃 (관리자) | 구현됨 |
| 애플 소셜 로그인 | 미구현 (`application.yml` registration 주석 처리) |

> 미구현/후속 항목은 [12장](#12-미구현--후속-과제) 참고.

---

## 1. 설계 메모

- Base path: `/api/v1/auth`
- 인증 방식: `Authorization: Bearer {accessToken}` (JWT, HS256)
- 로그인은 **브라우저 리다이렉트 기반 OAuth2 Authorization Code Flow** (Spring Security 기본 엔드포인트 사용). REST 로그인 엔드포인트는 없습니다.
- Access Token 만료 시 `POST /api/v1/auth/refresh` 로 재발급하며, 요청마다 Refresh Token 도 함께 교체됩니다 (RTR).
- 관리자 전용 API 는 `ROLE_ADMIN` 권한이 필요합니다. 최초 로그인 시 이메일이 `auth.admin-white-list` 에 있으면 `ADMIN`, 아니면 `USER` 로 생성됩니다.
- `Auth2Controller` 는 `ResponseEntity` 대신 `ApiResponse` 를 직접 반환하므로 **성공 응답의 HTTP 상태 코드는 생성·삭제 포함 항상 `200`** 입니다. (본문 `statusCode` 도 200)

### 1.1 JWT 클레임

| 클레임 | 값 |
|--------|-----|
| `sub` | 소셜 고유 ID (`providerId`, OIDC `sub`) |
| `provider` | `google` / `apple` |
| `iat` / `exp` | 발급·만료 시각 (Access `jwt.access-token-expiration`, Refresh `jwt.refresh-token-expiration`) |

- 서명: HS256, 시크릿은 `jwt.secret` (Base64 인코딩된 값)
- `JwtAuthenticationFilter` 가 `sub` + `provider` 로 `users` 를 조회해 인증 컨텍스트에 `User` 엔티티를 principal 로 넣습니다. (일치 유저 없으면 `404 USER_404`)

### 1.2 공통 응답 봉투

```json
{ "statusCode": 200, "message": "...", "data": { } }
```

- 성공 시 `error` 키 생략, 실패 시 `data` / `message` 생략:

```json
{ "statusCode": 401, "error": { "code": "TOKEN_401", "message": "유효하지 않은 토큰입니다." } }
```

---

## 2. 소셜 로그인 플로우

```
[클라이언트]                         [서버]                       [Google]
    |  GET /api/v1/auth/login-urls    |                              |
    |-------------------------------->|                              |
    |  { google: "...", apple: "..." }|                              |
    |<-------------------------------|                               |
    |  브라우저를 google URL 로 이동   |                              |
    |------------------------------------------------------------->  |
    |                                 |  로그인/동의 후 콜백           |
    |                                 |  GET /login/oauth2/code/google|
    |                                 |<-----------------------------|
    |                                 |  CustomOAuth2UserService:     |
    |                                 |   - 유저 saveOrUpdate         |
    |                                 |  OAuth2SuccessHandler:        |
    |                                 |   - Access/Refresh 발급       |
    |                                 |   - users.refresh_token 저장  |
    |  Access/Refresh 토큰이 담긴 HTML |                              |
    |<-------------------------------|                               |
```

### 2.1 로그인 시작 URL

`GET {SERVER_URL}/oauth2/authorization/google` (Spring Security 기본)

- `SERVER_URL` = `auth.server-url`
- URL 은 `GET /api/v1/auth/login-urls` 응답으로 제공됨

### 2.2 콜백 & 토큰 전달

- 콜백: `GET {SERVER_URL}/login/oauth2/code/{provider}` (Spring Security 기본)
- 성공 시 `OAuth2SuccessHandler` 가 **Access Token / Refresh Token 이 표시된 HTML 페이지**를 반환합니다. (JSON 아님 — 현재는 Swagger 테스트 편의용, [12장](#12-미구현--후속-과제) 참고)
- Refresh Token 은 `users.refresh_token` 에 평문 저장됨

---

## 3. `GET /api/v1/auth/login-urls` — 소셜 로그인 진입 URL 조회

- 인증 불필요 (`permitAll`, `SecurityConfig` 에 등록됨)

응답:

```json
{
  "statusCode": 200,
  "message": "소셜 로그인 URL 조회 성공",
  "data": {
    "google": "http://localhost:8080/oauth2/authorization/google",
    "apple": "http://localhost:8080/oauth2/authorization/apple"
  }
}
```

> `apple` URL 은 반환되지만 애플 registration 이 비활성 상태라 실제로는 동작하지 않습니다.

---

## 4. `POST /api/v1/auth/refresh` — 토큰 재발급 (RTR)

- 인증 불필요 (`permitAll`)
- Refresh Token 검증 → 새 Access/Refresh 발급 + `users.refresh_token` 교체

요청 바디:

```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." }
```

응답:

```json
{
  "statusCode": 200,
  "message": "토큰 재발급 및 로테이션 성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

| 실패 케이스 | 응답 |
|-------------|------|
| `refreshToken` 누락 | `401 TOKEN_401` (`INVALID_TOKEN`) |
| DB 저장 토큰과 불일치 (이미 로테이션됨 / 로그아웃됨) | `401 TOKEN_401` |
| 토큰의 유저가 존재하지 않음 | `404 USER_404` |
| Refresh Token 만료 | **의도상 `401 TOKEN_402`(`EXPIRED_TOKEN`)** — 단, 현재 코드는 `validateToken(String)` 이 예외를 삼키고 `boolean` 만 반환하는데 그 반환값을 확인하지 않아, 만료 검증이 사실상 동작하지 않음 ([12장](#12-미구현--후속-과제)) |

---

## 5. `GET /api/v1/auth/user` — 현재 로그인 유저 정보

- 인증 필요 (`isAuthenticated()`)

요청 헤더: `Authorization: Bearer {accessToken}`

응답:

```json
{
  "statusCode": 200,
  "message": "로그인 유저 정보 조회 성공",
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@gmail.com",
    "provider": "google",
    "role": "USER"
  }
}
```

---

## 6. `GET /api/v1/auth/{id}` — 특정 회원 조회

- 인증 필요 — **본인(`id` == 로그인 유저) 또는 `ROLE_ADMIN`**
- 권한 없으면 `403 AUTH_403`

| 경로 파라미터 | 타입 | 설명 |
|------|------|------|
| `id` | number | 조회할 회원 ID |

응답: `UserResponse` (5장과 동일 형태)

```json
{
  "statusCode": 200,
  "message": "회원 정보 조회 성공",
  "data": { "id": 2, "name": "김철수", "email": "kim@gmail.com", "provider": "google", "role": "USER" }
}
```

- 존재하지 않는 `id` → `404 USER_404`

---

## 7. 관리자 전용 API (`ROLE_ADMIN`)

권한 없으면 모두 `403 AUTH_403`.

### 7.1 `PATCH /api/v1/auth/role/{id}` — 유저 권한 수정

- 쿼리 파라미터: `role` (`USER` / `ADMIN`)

```
PATCH /api/v1/auth/role/2?role=ADMIN
```

응답:

```json
{ "statusCode": 200, "message": "유저 권한 수정 성공", "data": null }
```

### 7.2 `GET /api/v1/auth/all` — 전체 회원 목록

응답 (`List<User>` — **엔티티 직렬화**):

```json
{
  "statusCode": 200,
  "message": "전체 회원 목록 조회 성공",
  "data": [
    {
      "id": 1,
      "providerId": "1097...",
      "email": "hong@gmail.com",
      "name": "홍길동",
      "provider": "google",
      "role": "USER",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
    }
  ]
}
```

> ⚠️ 현재 `User` 엔티티를 그대로 반환해 `providerId`, `refreshToken` 이 노출됩니다. `UserResponse` 로 교체 필요 ([12장](#12-미구현--후속-과제)).

### 7.3 `DELETE /api/v1/auth/withdraw/{id}` — 회원 탈퇴

```json
{ "statusCode": 200, "message": "회원 탈퇴 성공", "data": null }
```

- 존재하지 않는 `id` → `404 USER_404`

---

## 8. `POST /api/v1/auth/logout/{id}` — 로그아웃

- 인증 필요 — **본인 또는 `ROLE_ADMIN`**
- `users.refresh_token` 을 `null` 로 만들어 이후 재발급을 차단합니다. (Access Token 은 만료 전까지 유효 — [12장](#12-미구현--후속-과제))

```json
{ "statusCode": 200, "message": "로그아웃 성공. 모든 토큰이 무효화되었습니다.", "data": null }
```

---

## 9. 에러 코드 (실제 `ErrorCode` 기준)

| HTTP | `error.code` | enum 상수 | 설명 |
|------|--------------|-----------|------|
| `401` | `TOKEN_401` | `INVALID_TOKEN` | 유효하지 않은 토큰 (서명·형식 오류, DB 불일치) |
| `401` | `TOKEN_402` | `EXPIRED_TOKEN` | 만료된 토큰 — 재로그인/재발급 필요 |
| `403` | `AUTH_403` | `ACCESS_DENIED` | 접근 권한 부족 (`@PreAuthorize` 실패) |
| `403` | `USER_403` | `USER_ACCESS_DENIED` | 해당 유저 관련 권한 없음 |
| `404` | `USER_404` | `USER_NOT_FOUND` | 존재하지 않는 유저 |
| `500` | `SERVER_ERROR` | `INTERNAL_SERVER_ERROR` | 서버 오류 |

- 토큰 없이 인증 필요 API 호출 시: `JwtAuthenticationFilter` / `SecurityConfig` 진입점에서 `401 TOKEN_401` (혹은 `request` 속성에 담긴 코드)

---

## 10. 데이터 모델

`users` 테이블 (`User` 엔티티, `BaseEntity` 미상속 — `created_at`/`updated_at` 없음):

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `id` | bigint | PK, identity | 내부 회원 ID |
| `provider_id` | varchar | not null, unique | 소셜 고유 ID (`sub`) |
| `email` | varchar | not null | 이메일 |
| `name` | varchar | not null | 이름 |
| `provider` | varchar | not null | `google` / `apple` |
| `role` | varchar(enum) | not null | `USER` / `ADMIN` |
| `refresh_token` | varchar(500) | nullable | 현재 유효한 Refresh Token (평문) |

`Role` enum: `USER("ROLE_USER")`, `ADMIN("ROLE_ADMIN")`

---

## 11. 접근 제어 요약

| 엔드포인트 | 권한 |
|-----------|------|
| `GET /api/v1/auth/login-urls` | 없음 (`permitAll`) |
| `POST /api/v1/auth/refresh` | 없음 (`permitAll`) |
| `GET /api/v1/auth/user` | 인증 |
| `GET /api/v1/auth/{id}` | 본인 또는 `ADMIN` |
| `PATCH /api/v1/auth/role/{id}` | `ADMIN` |
| `GET /api/v1/auth/all` | `ADMIN` |
| `DELETE /api/v1/auth/withdraw/{id}` | `ADMIN` |
| `POST /api/v1/auth/logout/{id}` | 본인 또는 `ADMIN` |

- `permitAll` 전역 목록(`SecurityConfig`): `/api/v1/auth/login-urls`, `/api/v1/auth/refresh`, Swagger, `/h2-console/**`, `/api/v1/spots/sync/**`, `/api/v1/routes/public`
- 그 외 `anyRequest().authenticated()`

---

## 12. 미구현 / 후속 과제

- [ ] 애플 소셜 로그인 (`application.yml` registration 활성화 + `AppleUserInfo` 검증)
- [ ] OAuth2 성공 응답을 HTML → **프론트 리다이렉트(딥링크) + 토큰 전달** 또는 JSON 으로 전환
- [ ] `GET /api/v1/auth/all` 응답을 `UserResponse` 로 교체 (`providerId`, `refreshToken` 노출 제거)
- [ ] `users.refresh_token` 평문 저장 → 해시 저장 검토
- [ ] Refresh Token 만료 검증 수정 — `refreshToken()` 이 `validateToken(String)` 의 `boolean` 반환값을 확인하지 않아 만료된 Refresh Token 도 통과할 수 있음
- [ ] Access Token 블랙리스트 / 짧은 만료로 로그아웃 즉시성 확보 (현재는 만료 전까지 Access 유효)
- [ ] 본인 기준 로그아웃(`/logout`, id 불필요) 및 회원 탈퇴(`/withdraw`) 엔드포인트
- [ ] `Auth2Controller` 응답을 `ResponseEntity` 로 바꿔 생성/삭제에 적절한 HTTP 상태 코드 부여
