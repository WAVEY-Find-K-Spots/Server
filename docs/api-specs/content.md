# Content API 명세서

콘텐츠(Content) 도메인 API 규격입니다. 응답 예시는 **현재 서버 구현** 기준입니다.

## 구현 상태

| 범위 | 이슈 | 상태 |
|------|------|------|
| 스팟 연결 콘텐츠 조회 (전체 / 카테고리) | #86 | 구현 |
| 스팟 연결 콘텐츠 미디어 카드(영상/앨범/트랙) 조회 | #103 | 구현 |

---

## 1. 공통

- 필드명 `camelCase`
- 공통 봉투: `{ statusCode, message, data }` / 실패 시 `error: { code, message }`
- 콘텐츠 `category`: `ARTIST` | `DRAMA` | `MOVIE` (작품 종류). 유튜브·앨범은 자식 테이블이며 별도 미디어 API 사용

---

## 2. 스팟 연결 콘텐츠

### 2.1 `GET /api/v1/spots/{spotId}/contents`

스팟에 `spot_contents`로 연결된 작품 목록을 반환합니다. (`GET /api/v1/contents`와 동일한 필터 패턴)

- 인증: **불필요** (공개 GET)
- path: `spotId` (필수)
- query: `category` (`ARTIST` | `DRAMA` | `MOVIE`, 선택 — 없으면 **전체**)

응답 `data`: 배열. 각 원소:

| 필드 | 타입 | 설명 |
|------|------|------|
| `contentId` | number | 콘텐츠 ID |
| `category` | string | `DRAMA` / `MOVIE` / `ARTIST` |
| `title` | string | 한글 제목 (`titleKo`) |

전체 조회 예시:

```http
GET /api/v1/spots/10/contents
```

```json
{
  "statusCode": 200,
  "message": "스팟 연결 콘텐츠 조회 성공",
  "data": [
    { "contentId": 1, "category": "DRAMA", "title": "도깨비" },
    { "contentId": 2, "category": "ARTIST", "title": "IU" }
  ]
}
```

카테고리별 조회 예시:

```http
GET /api/v1/spots/10/contents?category=DRAMA
```

```json
{
  "statusCode": 200,
  "message": "스팟 연결 콘텐츠 조회 성공",
  "data": [
    { "contentId": 1, "category": "DRAMA", "title": "도깨비" }
  ]
}
```

스팟 없음:

```json
{
  "statusCode": 404,
  "error": {
    "code": "SPOT_NOT_FOUND",
    "message": "존재하지 않는 장소입니다."
  }
}
```

> 유튜브·앨범·트랙 카드 묶음은 2.2 `GET /api/v1/spots/{spotId}/media` 참고. 본 API는 작품 목록만 줍니다.

---

### 2.2 `GET /api/v1/spots/{spotId}/media`

스팟에 연결된 **모든** 콘텐츠(2.1의 각 항목)의 유튜브 영상 / 스포티파이 앨범(수록 트랙 포함) / 단독 트랙을 콘텐츠별로 묶어서 반환합니다.

- 인증: **불필요** (공개 GET)
- path: `spotId` (필수)
- 숨김(`hidden=true`) 처리된 영상/앨범/트랙은 응답에서 제외됩니다.

응답 `data` (`SpotMediaResponse`):

```ts
{
  spotId: number
  contents: {
    contentId: number
    title: string          // Content.titleKo
    category: "ARTIST" | "DRAMA" | "MOVIE"
    videos: ContentVideoResponse[]      // content.md의 3장 참고
    albums: ContentAlbumResponse[]      // 수록 트랙(tracks) 포함
    tracks: ContentTrackResponse[]      // 앨범에 속하지 않는 단독 트랙
  }[]
}
```

예시:

```json
{
  "statusCode": 200,
  "message": "스팟 연결 콘텐츠 미디어 조회 성공",
  "data": {
    "spotId": 10,
    "contents": [
      {
        "contentId": 1,
        "title": "도깨비",
        "category": "DRAMA",
        "videos": [
          { "id": 1, "videoId": "abc123", "title": "도깨비 OST", "channelTitle": "tvN", "thumbnailUrl": "https://...", "durationSec": 240, "kind": "LONG", "hidden": false }
        ],
        "albums": [],
        "tracks": []
      }
    ]
  }
}
```

연결된 콘텐츠가 없으면 `contents: []`. 스팟이 없으면 `404 SPOT_NOT_FOUND`.
