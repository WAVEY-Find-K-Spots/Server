# Spot API 명세서

장소(Spot) 도메인의 API 규격입니다. 스팟 CRUD + 외부 공공데이터 동기화(sync)로 구성됩니다.
응답 예시는 **현재 서버 구현 형태**를 기준으로 작성했습니다.

## 구현 상태

| 범위 | 상태 |
|------|------|
| 스팟 CRUD (`/api/v1/spots`) | 구현됨 |
| 지도 범위 조회 (`/api/v1/spots/map`) | 구현됨 |
| 외부 데이터 동기화 (`/api/v1/spots/sync`) — 한국관광공사 문화유산, 미디어콘텐츠 촬영지, 썸네일 백필 | 구현됨 |

> 미구현/후속 항목은 [10장](#10-미구현--후속-과제) 참고.

---

## 1. 설계 메모

- Base path: `/api/v1/spots`
- 공통 응답 봉투는 [routes.md](./routes.md#12-공통-응답-봉투) 와 동일 (`statusCode` / `message` / `data`, 성공 시 `error` 생략)
- 좌표: `latitude` `DECIMAL(10,8)`, `longitude` `DECIMAL(11,8)` (WGS84)
- `category` enum: `K_DRAMA` / `K_POP` / `K_HERITAGE` / `K_MOVIE`
- `sourceType` enum: 외부 출처 (`TOUR_API` 등)
- 인증:
  - `/api/v1/spots/sync/**` → **인증 불필요** (`SecurityConfig` permitAll, 배치/운영용)
  - 그 외 `/api/v1/spots/**` → 인증 필요 (`Authorization: Bearer {accessToken}`)
  - 스팟 CRUD 에 별도 역할(ADMIN) 제한 없음 — 인증된 사용자면 생성/수정/삭제 가능 ([10장](#10-미구현--후속-과제))
- CRUD 컨트롤러는 `ResponseEntity` 를 사용하므로 생성은 `201`, 그 외 성공은 `200`

---

## 2. `POST /api/v1/spots` — 장소 생성

요청 바디:

```json
{
  "regionId": 1,
  "name": "경복궁",
  "category": "K_HERITAGE",
  "address": "서울 종로구 사직로 161",
  "latitude": 37.579617,
  "longitude": 126.977041,
  "description": "조선의 법궁",
  "openingHours": "09:00-18:00",
  "closedDays": "화요일",
  "tel": "02-3700-3900",
  "thumbnailUrl": "https://.../gyeongbokgung.jpg",
  "sourceType": "TOUR_API",
  "externalContentId": "126508"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `regionId` | number | Y | 지역 ID |
| `name` | string | Y | 장소명 |
| `category` | enum | Y | `K_DRAMA` / `K_POP` / `K_HERITAGE` / `K_MOVIE` |
| `latitude` | number | Y | -90 ~ 90 |
| `longitude` | number | Y | -180 ~ 180 |
| `sourceType` | enum | Y | 외부 출처 |
| `address` / `description` / `openingHours` / `closedDays` / `tel` / `thumbnailUrl` / `externalContentId` | string | N | |

응답 (201, `SpotResponse`):

```json
{
  "statusCode": 201,
  "message": "장소 생성 성공",
  "data": {
    "spotId": 101,
    "regionId": 1,
    "name": "경복궁",
    "category": "K_HERITAGE",
    "address": "서울 종로구 사직로 161",
    "latitude": 37.57961700,
    "longitude": 126.97704100,
    "description": "조선의 법궁",
    "openingHours": "09:00-18:00",
    "closedDays": "화요일",
    "tel": "02-3700-3900",
    "thumbnailUrl": "https://.../gyeongbokgung.jpg",
    "sourceType": "TOUR_API",
    "externalContentId": "126508",
    "avgRating": 0.0,
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
}
```

- 동일 외부 데이터 중복 → `409 SPOT_EXTERNAL_DATA_ALREADY_EXISTS`

---

## 3. `GET /api/v1/spots/{spotId}` — 장소 단건 조회

응답: `SpotResponse` (2장과 동일 형태). 없으면 `404 SPOT_NOT_FOUND`.

---

## 4. `GET /api/v1/spots` — 장소 목록 조회

쿼리 파라미터 (모두 선택):

| 필드 | 타입 | 설명 |
|------|------|------|
| `category` | enum | 카테고리 필터 |
| `regionId` | number | 지역 필터 |

응답 (`List<SpotListResponse>`):

```json
{
  "statusCode": 200,
  "message": "장소 목록 조회 성공",
  "data": [
    {
      "spotId": 101,
      "name": "경복궁",
      "category": "K_HERITAGE",
      "address": "서울 종로구 사직로 161",
      "latitude": 37.57961700,
      "longitude": 126.97704100,
      "thumbnailUrl": "https://.../a.jpg",
      "avgRating": 0.0
    }
  ]
}
```

---

## 5. `GET /api/v1/spots/map` — 지도 범위 조회

지정한 위/경도 범위(bounding box) 안의 장소를 조회합니다.

| 쿼리 파라미터 | 타입 | 필수 | 범위 |
|------|------|------|------|
| `minLat` | number | Y | -90 ~ 90 |
| `maxLat` | number | Y | -90 ~ 90 |
| `minLng` | number | Y | -180 ~ 180 |
| `maxLng` | number | Y | -180 ~ 180 |

응답: `List<SpotListResponse>` (4장과 동일). 잘못된 범위 → `400 SPOT_INVALID_MAP_BOUNDS` / `400 COMMON_INVALID_PARAMETER`.

---

## 6. `PATCH /api/v1/spots/{spotId}` — 장소 수정 / `DELETE` — 삭제

### 6.1 수정

요청 바디 (전달된 필드만 갱신, 모두 선택):

```json
{ "name": "경복궁", "address": "서울 종로구 사직로 161", "latitude": 37.579617, "longitude": 126.977041, "thumbnailUrl": "https://.../new.jpg" }
```

응답: `SpotResponse`. 없으면 `404 SPOT_NOT_FOUND`.

### 6.2 삭제

```json
{ "statusCode": 200, "message": "장소 삭제 성공", "data": null }
```

---

## 7. 외부 데이터 동기화 API (`/api/v1/spots/sync`)

- **인증 불필요** (permitAll). 운영/배치 용도.
- 외부 인증키(`external-api.service-key`) 미설정 시 `400 SPOT_EXTERNAL_API_KEY_MISSING`
- 외부 API 호출 실패 시 `502 SPOT_EXTERNAL_API_REQUEST_FAILED`
- 기존 데이터는 `sourceType` + `externalContentId` 기준으로 비교해 **변경된 필드만 갱신**, 신규만 추가

공통 응답 (`SpotSyncResponse`):

```json
{
  "statusCode": 200,
  "message": "...",
  "data": {
    "requestedCount": 100,
    "totalCount": 3421,
    "pageCount": 1,
    "savedCount": 42,
    "updatedCount": 8,
    "unchangedCount": 48,
    "skippedCount": 2
  }
}
```

| 엔드포인트 | 쿼리 파라미터 | 설명 |
|-----------|---------------|------|
| `POST /heritage` | `pageNo`(기본 1), `numOfRows`(기본 100) | 한국관광공사 문화유산 장소 지정 페이지 → `K_HERITAGE` 저장 |
| `POST /heritage/all` | `numOfRows`(기본 100) | 문화유산 장소 전체 페이지 순회 |
| `POST /media-locations` | `category`(선택), `page`(기본 1), `perPage`(기본 100) | 미디어콘텐츠 촬영지 지정 페이지. drama→`K_DRAMA`, artist→`K_POP`, movie→`K_MOVIE`. 저장 전 TourAPI 검색으로 대표 썸네일 보강 |
| `POST /media-locations/all` | `category`(선택), `perPage`(기본 100) | 미디어콘텐츠 촬영지 전체 순회 |
| `POST /media-locations/thumbnails` | `category`(선택), `limit`(기본 100) | 이미 저장된 촬영지 중 `thumbnailUrl` 이 빈 데이터를 TourAPI 검색 결과로 백필 (점수 기준 이상만) |

> 스케줄러(`spot.sync.scheduler.*`)로 주기 실행도 가능 (기본 비활성).

---

## 8. 에러 코드 (실제 `ErrorCode` 기준)

| HTTP | `error.code` | enum 상수 | 설명 |
|------|--------------|-----------|------|
| `400` | `COMMON_INVALID_PARAMETER` | `COMMON_INVALID_PARAMETER` | 요청 파라미터 오류 |
| `400` | `SPOT_INVALID_MAP_BOUNDS` | `SPOT_INVALID_MAP_BOUNDS` | 잘못된 지도 범위 |
| `400` | `SPOT_EXTERNAL_API_KEY_MISSING` | `SPOT_EXTERNAL_API_KEY_MISSING` | 외부 API 인증키 미설정 |
| `404` | `SPOT_NOT_FOUND` | `SPOT_NOT_FOUND` | 존재하지 않는 장소 |
| `409` | `SPOT_EXTERNAL_DATA_ALREADY_EXISTS` | `SPOT_EXTERNAL_DATA_ALREADY_EXISTS` | 이미 등록된 외부 장소 데이터 |
| `502` | `SPOT_EXTERNAL_API_REQUEST_FAILED` | `SPOT_EXTERNAL_API_REQUEST_FAILED` | 외부 장소 데이터 요청 실패 |

---

## 9. 데이터 모델

`spots` 테이블 (`Spot` 엔티티, PK 컬럼 `spot_id`):

| 컬럼 | 타입 | 제약 |
|------|------|------|
| `spot_id` | bigint | PK |
| `region_id` | bigint | not null |
| `name` | varchar(255) | not null |
| `category` | varchar(20)(enum) | not null |
| `address` | varchar(500) | |
| `latitude` / `longitude` | decimal(10,8) / (11,8) | not null |
| `description` | text | |
| `opening_hours` / `closed_days` / `tel` | varchar | |
| `thumbnail_url` | varchar(500) | |
| `source_type` | varchar(30)(enum) | not null |
| `external_content_id` | varchar(100) | |
| `avg_rating` | double | not null, default 0.0 |
| `created_at` / `updated_at` | timestamp | `BaseEntity` |

인덱스: `region_id`, `category`, `(latitude, longitude)`, `(source_type, external_content_id)`

---

## 10. 미구현 / 후속 과제

- [ ] 스팟 CRUD 권한 제한 (현재 인증된 누구나 생성/수정/삭제 가능 → `ADMIN` 제한 검토)
- [ ] `GET /api/v1/spots` 페이징 (현재 `List` 전체 반환)
- [ ] `excludeRouteId` 쿼리 파라미터 (루트 탭 SpotPicker 용, [routes.md](./routes.md) 참고)
- [ ] `avgRating` 산출 로직 (현재 항상 0.0)
