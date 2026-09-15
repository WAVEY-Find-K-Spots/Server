# Content API 명세서

콘텐츠(Content) 도메인 API 규격입니다. 응답 예시는 **현재 서버 구현** 기준입니다.

## 구현 상태

| 범위 | 이슈 | 상태 |
|------|------|------|
| 스팟 연결 콘텐츠 조회 (전체 / 카테고리) | #86 | 구현 |

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

> 유튜브·앨범·트랙 카드 묶음은 `GET /api/v1/spots/{spotId}/media` (후속). 본 API는 작품 목록만 줍니다.
