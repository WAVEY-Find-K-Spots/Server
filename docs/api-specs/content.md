# Content API 명세서

유튜브 / 스포티파이 콘텐츠 관리 도메인의 API 규격입니다.
응답 예시는 **현재 서버 구현 형태**를 기준으로 작성했습니다.

## 구현 상태

| 범위 | 상태 |
|------|------|
| 유튜브 콘텐츠 CRUD + 검색 | 구현됨 |
| 스포티파이 콘텐츠 CRUD + 검색 | 구현됨 |
| URL 파싱으로 `externalId` / `thumbnailUrl` 자동 추출 | 구현됨 |

> 미구현/후속 항목은 [7장](#7-미구현--후속-과제) 참고.

---

## 1. 설계 메모

- Base path: `/api/v1/contents`
- 공통 응답 봉투는 [routes.md](./routes.md#12-공통-응답-봉투) 와 동일
- 플랫폼별로 엔드포인트가 분리되어 있습니다 (`/youtube`, `/spotify`). `platform` enum: `YOUTUBE` / `SPOTIFY`
- 등록 요청은 `url` 만 받으면 서버가 해당 URL 을 파싱해 `externalId`(영상/트랙 ID)와 `thumbnailUrl` 을 채웁니다.
- 인증: `/api/v1/contents/**` 전부 인증 필요 (`Authorization: Bearer {accessToken}`). 역할(ADMIN) 제한 없음.
- 컨트롤러가 `ResponseEntity.ok(...)` 를 사용하므로 **등록·수정·삭제 모두 HTTP `200`** (`201` 아님).

---

## 2. 공통 요청/응답

### 2.1 `ContentRequest`

```json
{
  "title": "BTS - Dynamite",
  "url": "https://www.youtube.com/watch?v=gdZLi9oWNZg",
  "description": "플레이리스트에 저장하고 싶은 콘텐츠"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | string | Y | 콘텐츠 제목 |
| `url` | string | Y | 유튜브 또는 스포티파이 URL |
| `description` | string | N | 설명 |

### 2.2 `ContentResponse`

```json
{
  "contentId": 1,
  "platform": "YOUTUBE",
  "title": "밤양갱",
  "description": "플레이리스트에 저장하고 싶은 콘텐츠",
  "externalId": "dQw4w9WgXcQ",
  "thumbnailUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
  "createdAt": "2026-03-23T10:00:00",
  "updatedAt": "2026-03-23T11:00:00"
}
```

---

## 3. 유튜브 콘텐츠

| 메서드 · 엔드포인트 | 설명 | 요청 | 응답 |
|---------------------|------|------|------|
| `POST /api/v1/contents/add/youtube` | 등록 | `ContentRequest` | `200`, `ContentResponse` |
| `GET /api/v1/contents/search/youtube?keyword=` | 검색 (keyword 선택, 미입력 시 전체) | — | `200`, `List<ContentResponse>` |
| `PUT /api/v1/contents/update/youtube/{contentId}` | 수정 | `ContentRequest` | `200`, `ContentResponse` |
| `DELETE /api/v1/contents/delete/youtube/{contentId}` | 삭제 | — | `200`, `data: null` |

등록 응답 예:

```json
{
  "statusCode": 200,
  "message": "유튜브 콘텐츠 등록 성공",
  "data": {
    "contentId": 1,
    "platform": "YOUTUBE",
    "title": "BTS - Dynamite",
    "description": null,
    "externalId": "gdZLi9oWNZg",
    "thumbnailUrl": "https://i.ytimg.com/vi/gdZLi9oWNZg/hqdefault.jpg",
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
}
```

실패:

| 케이스 | 응답 |
|--------|------|
| `url` 누락 | `400 CONTENT_400_URL` (`CONTENT_URL_REQUIRED`) |
| 유효하지 않은 유튜브 URL | `400 CONTENT_400_YOUTUBE_URL` (`INVALID_YOUTUBE_URL`) |
| 이미 등록된 콘텐츠 | `409 CONTENT_409` (`CONTENT_ALREADY_EXISTS`) |
| 썸네일 자동 조회 실패 | `502 CONTENT_502_THUMBNAIL` (`CONTENT_THUMBNAIL_RESOLVE_FAILED`) |
| 존재하지 않는 `contentId` (수정/삭제) | `404 CONTENT_404` (`CONTENT_NOT_FOUND`) |

---

## 4. 스포티파이 콘텐츠

유튜브와 동일 구조, 경로만 `/spotify`:

| 메서드 · 엔드포인트 | 설명 |
|---------------------|------|
| `POST /api/v1/contents/add/spotify` | 등록 |
| `GET /api/v1/contents/search/spotify?keyword=` | 검색 |
| `PUT /api/v1/contents/update/spotify/{contentId}` | 수정 |
| `DELETE /api/v1/contents/delete/spotify/{contentId}` | 삭제 |

- 유효하지 않은 스포티파이 URL → `400 CONTENT_400_SPOTIFY_URL` (`INVALID_SPOTIFY_URL`)
- 나머지 에러는 유튜브와 동일

---

## 5. 에러 코드 (실제 `ErrorCode` 기준)

| HTTP | `error.code` | enum 상수 | 설명 |
|------|--------------|-----------|------|
| `400` | `CONTENT_400_URL` | `CONTENT_URL_REQUIRED` | 콘텐츠 URL 필수 |
| `400` | `CONTENT_400_YOUTUBE_URL` | `INVALID_YOUTUBE_URL` | 유효하지 않은 유튜브 URL |
| `400` | `CONTENT_400_SPOTIFY_URL` | `INVALID_SPOTIFY_URL` | 유효하지 않은 스포티파이 URL |
| `404` | `CONTENT_404` | `CONTENT_NOT_FOUND` | 존재하지 않는 콘텐츠 |
| `409` | `CONTENT_409` | `CONTENT_ALREADY_EXISTS` | 이미 등록된 콘텐츠 |
| `502` | `CONTENT_502_THUMBNAIL` | `CONTENT_THUMBNAIL_RESOLVE_FAILED` | 썸네일 자동 조회 실패 |

---

## 6. 데이터 모델

`contents` 테이블 (`Content` 엔티티, PK 컬럼 `content_id`):

| 컬럼 | 타입 | 제약 |
|------|------|------|
| `content_id` | bigint | PK |
| `platform` | varchar(20)(enum) | not null |
| `title` | varchar(255) | not null |
| `description` | text | |
| `external_id` | varchar(255) | not null |
| `thumbnail_url` | varchar(500) | not null |
| `created_at` / `updated_at` | timestamp | `BaseEntity` |

---

## 7. 미구현 / 후속 과제

- [ ] 콘텐츠 ↔ 스팟/루트 연관 (현재 독립 엔티티, `kContentTitle` 등에서 필요 — [routes.md](./routes.md) 참고)
- [ ] 검색 페이징 (현재 `List` 전체 반환)
- [ ] 등록/수정/삭제 응답에 적절한 HTTP 상태 코드 (`201` 등)
- [ ] 소유자 개념 (현재 전역 공유, 누구나 수정/삭제 가능)
