# Upload API 명세서

파일(이미지 등) 업로드를 위한 범용 도메인입니다. 특정 도메인(프로필, 게시물 등)에 종속되지 않고
`category`로 용도를 구분하는 구조입니다. 실제 파일 바이너리는 앱 서버를 거치지 않고
클라이언트가 스토리지(S3 호환)에 presigned URL로 직접 업로드합니다.

## 구현 상태

| 범위 | 상태 |
|------|------|
| `PROFILE` 카테고리 (프로필 사진) | 구현됨 |
| `BADGE` 카테고리 (배지 이미지) | 구현됨 |
| 그 외 카테고리 (게시물 등) | 미구현 — `UploadCategory` enum에 추가 필요 |
| 이미지 조회(읽기) — private 버킷 대응 (#100) | 구현됨 |

## 0. 버킷은 private — 읽기는 매 응답마다 재서명

이 버킷은 공개 읽기가 아닌 **private**입니다. 업로드 시 반환하는 `fileUrl`(`{publicBaseUrl}/{key}`)은 고정 URL이지만 그 자체로는 접근 불가(`AccessDenied`)합니다.
실제 조회 가능한 URL은 **응답을 내려줄 때마다 새로 서명한 presigned GET URL**입니다 — `UploadService.resolveAccessUrl(storedUrl)`이 처리하며, `UserResponse.profileImageUrl`/배지 응답의 `imageUrl` 등에 적용됩니다.

- 프론트는 `profileImageUrl`/`imageUrl` 값을 **캐시하지 말고 매 API 응답에서 받은 값을 그대로** `<img src>`에 사용해야 합니다. 이전 응답에서 받은 URL은 `storage.presigned-get-ttl-seconds`(기본 3600초) 이후 만료됩니다.
- 우리 버킷 소속이 아닌 URL(예: TourAPI 스팟 이미지 등 외부 CDN)은 그대로 통과되어 영향 없습니다.

---

## 1. presigned URL 발급

```
POST /api/v1/uploads/presigned-url
Authorization: Bearer {accessToken}
Content-Type: application/json
```

### Request `PresignedUploadRequest`

```ts
{
  category: "PROFILE" | "BADGE"     // @NotNull, UploadCategory enum
  contentType: string     // @NotBlank, 예: "image/png"
}
```

허용되는 `contentType` (카테고리 공통, `PROFILE`/`BADGE` 동일):
- `image/jpeg` → `.jpg`
- `image/png` → `.png`
- `image/webp` → `.webp`

목록에 없는 contentType이면 `COMMON_INVALID_FILE_TYPE` 에러.

### Response `PresignedUploadResponse`

```ts
{
  uploadUrl: string   // 이 URL로 PUT 업로드
  fileUrl: string     // 업로드 완료 후 최종 접근 URL (다른 API에 이 값을 전달)
  expiresAt: string   // ISO-8601 Instant, uploadUrl 유효 기한
}
```

내부적으로 저장 키는 `{category}/{userId}/{UUID}.{ext}` 형태로 생성되며, 소유자(userId) 기준으로 경로가 구분됩니다.

---

## 2. 클라이언트 → 스토리지 직접 업로드

```
PUT {uploadUrl}
Content-Type: {presigned 발급 시 넘긴 contentType과 동일해야 함}
Body: 파일 바이너리
```

- 앱 서버 API가 아닙니다 (백엔드 인증 헤더 불필요, presigned URL 자체가 인증 역할).
- `expiresAt` 이전에 업로드 완료해야 합니다.
- 성공 시 이 업로드 자체에 대한 응답은 스토리지 쪽 표준 응답(보통 200/204)이며, 앱 서버는 관여하지 않습니다.

---

## 3. 업로드 확정 (소유권 검증 + 실제 리소스에 반영)

업로드된 파일을 실제 유저/게시물 등의 리소스에 연결하는 단계는 **카테고리별로 별도 API**에서 처리합니다.
Upload 도메인 자체는 확정 API를 제공하지 않고, `UploadService.validateOwnedUrl(category, ownerId, fileUrl)`을
각 도메인 서비스가 호출해 소유권만 검증합니다.

예: 프로필 사진 확정은 [`user.md`](./user.md#3-프로필-사진-업로드변경)의 `PATCH /api/v1/auth/user/photo` 참고.

**검증 규칙**: `fileUrl`이 `{publicBaseUrl}/{category.keyPrefix()}/{ownerId}/`로 시작해야 함. 아니면 `UPLOAD_INVALID_FILE_URL` 에러.

---

## 4. 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| `COMMON_INVALID_FILE_TYPE` | 400 | 카테고리에서 허용하지 않는 contentType |
| `COMMON_FILE_SIZE_EXCEEDED` | 400 | 파일 용량 제한 초과 |
| `COMMON_FILE_EMPTY` | 400 | 파일이 비어있거나 유효하지 않음 |
| `UPLOAD_INVALID_FILE_URL` | 400 | 확정하려는 fileUrl이 본인 소유 업로드 경로가 아님 |

---

## 5. 미구현 / 후속 과제

- `UploadCategory`에 `PROFILE`/`BADGE` 외 카테고리 추가 (게시물 이미지 등)
- 업로드 실패/만료 시 클라이언트 재시도 가이드 (현재 문서화 안 됨)
- 관리자가 Spot 이미지를 이 업로드 플로우로 올리게 되면, `SpotResponse.imageUrl`에도 `resolveAccessUrl()` 적용 필요 (현재 Spot 이미지는 대부분 TourAPI 등 외부 URL이라 미적용)
