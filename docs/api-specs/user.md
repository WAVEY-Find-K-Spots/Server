# User API 명세서

유저(User) 도메인 — 로그인/조회/탈퇴 등 계정 관련 API입니다.
소셜 로그인(OAuth) 자체의 흐름은 별도 문서를 참고하고, 여기서는 로그인 이후 유저 리소스 API를 다룹니다.

## 구현 상태

| 범위 | PR | 상태 |
|------|-----|------|
| 소셜 로그인 코드 교환 방식(C안) | #60 | 머지됨 |
| isNewUser 플래그 추가 | #79 | 리뷰 대기 |
| 프로필 사진 업로드/확정 | #83 | 리뷰 대기 |
| nickname / countryCode / language 프로필 확장 | #95 | 구현 완료 |

---

## 1. 설계 메모

- 모든 요청·응답 필드명은 `camelCase` 기준.
- 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더 사용.
- Base path: `/api/v1/auth`
- `provider`는 enum이 아니라 문자열(`"google"` / `"apple"` / `"kakao"`).
- `email`/`name`은 로그인 시마다 OAuth 제공자 응답으로 계속 덮어써지는 서버 동기화 값이라 **수정 API가 없고 읽기 전용**입니다. "닉네임"을 표시/수정하려면 반드시 `nickname` 필드를 쓰세요(`name`을 재사용하면 다음 로그인 때 값이 사라집니다).

### 1.1 공통 응답 봉투

```json
{
  "statusCode": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { }
}
```

실패 시:

```json
{
  "statusCode": 400,
  "error": {
    "code": "UPLOAD_INVALID_FILE_URL",
    "message": "업로드된 파일 URL이 유효하지 않습니다."
  }
}
```

---

## 2. 로그인 유저 정보 조회

```
GET /api/v1/auth/user
Authorization: Bearer {accessToken}
```

### Response `UserResponse`

```ts
{
  id: number
  name: string             // OAuth 표시 이름, 읽기 전용(로그인마다 재동기화됨)
  email: string             // 읽기 전용(OAuth 동기화)
  provider: string        // "google" | "apple" | "kakao"
  role: "USER" | "ADMIN"  // Role enum
  profileImageUrl: string | null
  nickname: string | null       // 유저가 직접 설정하는 별도 표시명
  countryCode: CountryCode | null   // "KR" | "CN" | "VN" | ... (16개국, 3.2 참고)
  language: "KO" | "EN"          // 기본값 KO
}
```

---

## 3. 프로필 수정

### 3.1 닉네임/국적/언어 수정

```
PATCH /api/v1/auth/user
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request `UserProfileUpdateRequest`** (전달한 필드만 반영, 부분 수정)

```ts
{
  nickname?: string       // max 50
  countryCode?: CountryCode  // 3.2의 enum 값만 허용
  language?: "KO" | "EN"
}
```

**Response**: `200 UserResponse`

`email`/`name`은 이 API로 수정할 수 없습니다(요청 필드 자체가 없음) — OAuth 재로그인 시에만 서버가 동기화합니다.

### 3.2 `CountryCode` enum

`domain/user/enums/CountryCode.java`. 리뷰 도메인의 국가 표시와 동일한 enum을 재사용합니다.

`KR`, `CN`, `VN`, `TH`, `UZ`, `NP`, `KH`, `ID`, `PH`, `MM`, `MN`, `US`, `KZ`, `LK`, `RU`, `BD`

---

## 4. 프로필 사진 업로드/변경

사진 업로드는 서버를 경유하지 않고 클라이언트가 스토리지(S3 호환)에 직접 PUT 업로드한 뒤, 서버에 URL을 확정(confirm)하는 3단계 흐름입니다. presigned URL 발급 자체는 [`upload.md`](./upload.md)의 범용 업로드 API를 사용합니다.

### Step 1) presigned URL 발급 — [upload.md](./upload.md#1-presigned-url-발급) 참고

```
POST /api/v1/uploads/presigned-url
Body: { "category": "PROFILE", "contentType": "image/png" }
→ { uploadUrl, fileUrl, expiresAt }
```

### Step 2) 클라이언트 → 스토리지 직접 업로드

```
PUT {uploadUrl}
Content-Type: {Step1과 동일한 contentType}
Body: 파일 바이너리
```

- 앱 서버를 거치지 않습니다.
- `expiresAt` 이전에 업로드를 완료해야 합니다.

### Step 3) 업로드 확정 (프로필 이미지로 반영)

```
PATCH /api/v1/auth/user/photo
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request `PhotoConfirmRequest`**

```ts
{
  photoUrl: string   // @NotBlank, Step1에서 받은 fileUrl 그대로
}
```

**Response**: `200 UserResponse` (`profileImageUrl` 갱신됨)

**검증 로직**: `photoUrl`이 본인이 Step1에서 발급받은 경로(`{publicBaseUrl}/profile/{userId}/...`)로 시작하지 않으면 `UPLOAD_INVALID_FILE_URL` 에러. 즉 임의의 외부 URL이나 타인의 업로드 경로는 확정 불가.

---

## 5. 기타 유저 API

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/v1/auth/login-urls` | 소셜 로그인 URL 목록 | 비인증 |
| POST | `/api/v1/auth/exchange` | 로그인 코드 교환 → 토큰 발급 | 비인증 |
| GET | `/oauth2/authorization/{provider}?platform=app` | 네이티브 앱용 로그인 시작 (딥링크 콜백) | 비인증 |
| POST | `/api/v1/auth/refresh` | 액세스 토큰 갱신 | 비인증(리프레시 토큰 필요) |
| GET | `/api/v1/auth/{id}` | 특정 유저 조회 | 본인 또는 ADMIN |
| PATCH | `/api/v1/auth/role/{id}?role={ROLE}` | 유저 role 변경 | ADMIN |
| GET | `/api/v1/auth/all` | 전체 유저 목록 | ADMIN |
| DELETE | `/api/v1/auth/withdraw` | 회원 탈퇴 | 로그인 유저 |
| POST | `/api/v1/auth/logout` | 로그아웃 | 로그인 유저 |

---

## 6. 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| `USER_NOT_FOUND` | 404 | 해당 유저를 찾을 수 없음 |
| `USER_ACCESS_DENIED` | 403 | 해당 유저 관련 권한 없음 |
| `OAUTH_INVALID_USER_INFO` | 400 | 소셜 로그인 사용자 정보를 확인할 수 없음 |
| `OAUTH_PROFILE_REQUIRED` | 400 | 신규 가입 시 프로필 이름 제공 동의 필요 |
| `UPLOAD_INVALID_FILE_URL` | 400 | 프로필 사진 URL이 본인 업로드 경로가 아님 (사진 확정 시) |

> 참고: 과거 `USER_INVALID_PHOTO_URL`은 리팩터링으로 범용 `UPLOAD_INVALID_FILE_URL`로 대체되어 더 이상 존재하지 않습니다.

---

## 6-1. 네이티브 앱(Capacitor) 로그인 플로우 (#122)

인앱 웹뷰에서는 구글이 로그인을 차단하므로, 앱은 시스템 브라우저(`@capacitor/browser` 등)로 로그인을 열고 딥링크로 콜백을 받아야 한다.

1. 앱이 `GET /api/v1/auth/login-urls`로 받은 URL 뒤에 `?platform=app`을 붙여 시스템 브라우저로 오픈
   - 예: `{serverUrl}/oauth2/authorization/google?platform=app`
2. 로그인 완료 후 서버가 `wavey://oauth/callback?code={loginCode}` (실패 시 `?error={code}`)로 리다이렉트
3. 앱이 해당 딥링크 스킴을 수신하여 `code`를 `POST /api/v1/auth/exchange`로 교환

`platform=app` 파라미터가 없으면 기존과 동일하게 `auth.frontend-redirect-uri`(웹 프론트)로 리다이렉트된다. 리다이렉트 목적지는 서버에 미리 등록된 두 값(`auth.frontend-redirect-uri` / `auth.app-redirect-uri`) 중 하나로만 고정되어 오픈 리다이렉트 위험이 없다.

---

## 7. 미구현 / 후속 과제

- 프로필 사진 삭제(기본 이미지로 되돌리기) API
- 찜(저장한 스팟) 토글 + 목록 조회는 별도 이슈(#96, spot 도메인)
