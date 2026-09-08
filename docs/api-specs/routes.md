# Route API 명세서

루트(Route) 도메인의 전체 API 규격 + 클라이언트 `루트` 탭 화면 매핑을 한 문서로 정리한 것입니다.
응답 예시는 **현재 서버 구현 형태**를 기준으로 작성했습니다.

## 구현 상태

| 범위 | PR | 상태 |
|------|-----|------|
| 공개 루트 조회 비인증 허용 | #28 (`fix/#27`) | 리뷰 중 |
| 루트 상세 조회에 스팟 상세정보 포함 (`spotCount`, 스팟 필드 조인) | #31 (`feat/#30`) | 리뷰 중 |
| 경로 계산(Directions) API | #33 (`feat/#32`) | 리뷰 중 |

> 미구현/후속 항목은 [10장](#10-미구현--후속-과제) 참고.

---

## 1. 설계 메모

- 모든 요청·응답 필드명은 `camelCase` 기준.
- 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더 사용.
- 루트는 `visibility`(`PUBLIC` / `PRIVATE`)로 공개 여부 관리.
- 루트에 속한 스팟은 `sequenceOrder`(1부터 시작하는 정수)로 순서 관리, 순서 변경은 PATCH.
- 외부 응답에서 내부 PK(`userId`, `routeId`, `routeSpotId`, `spotId`)는 그대로 노출.
- 좌표: `latitude` / `longitude` (WGS84). `Spot` 은 위도 `DECIMAL(10,8)`, 경도 `DECIMAL(11,8)` — 소수점 8자리.
- 지도: 카카오맵. 길찾기 엔진: Tmap 오픈API.

### 1.1 공통 요청 헤더

- `Content-Type: application/json` — JSON 바디가 있는 API
- `Authorization: Bearer {accessToken}` — 인증이 필요한 API

### 1.2 공통 응답 봉투

```json
{
  "statusCode": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { }
}
```

- `error` 는 `@JsonInclude(NON_NULL)` 이라 **성공 응답에서는 키 자체가 생략**됩니다.
- 실패 시에는 `data` / `message` 가 생략되고 `error` 만 포함:

```json
{
  "statusCode": 404,
  "error": {
    "code": "ROUTE_NOT_FOUND",
    "message": "해당 루트를 찾을 수 없습니다."
  }
}
```

---

## 2. 루트 CRUD API

### 2.1 `GET /api/v1/routes` — 내 루트 목록

로그인 사용자 본인의 루트 목록을 반환합니다.

- 쿼리 파라미터: `visibility` (`PUBLIC` / `PRIVATE`, 선택 — 미입력 시 전체)
- **페이징 없음** (`data` 가 곧 배열)

요청 헤더: `Authorization: Bearer {accessToken}`

응답:

```json
{
  "statusCode": 200,
  "message": "내 루트 목록 조회 성공",
  "data": [
    {
      "routeId": 1,
      "name": "경복궁 궁궐 투어",
      "description": null,
      "visibility": "PRIVATE",
      "spotCount": 3,
      "createdAt": "2025-04-01T10:00:00",
      "updatedAt": "2025-04-10T15:30:00"
    }
  ]
}
```

---

### 2.2 `GET /api/v1/routes/public` — 공개 루트 조회

모든 사용자의 공개(`PUBLIC`) 루트를 조회합니다. **인증 없이 접근 가능** (#28).

- 쿼리 파라미터: `page`(기본 0), `size`(기본 20)
- `regionId` 필터는 **미구현** (10장)
- 응답 `data` 는 Spring `Page` 직렬화 원형

응답:

```json
{
  "statusCode": 200,
  "message": "공개 루트 조회 성공",
  "data": {
    "content": [
      {
        "routeId": 1,
        "name": "서울 야경 루트",
        "description": "한강과 남산을 잇는 야경 코스",
        "visibility": "PUBLIC",
        "spotCount": 4,
        "createdAt": "2025-04-01T10:00:00",
        "updatedAt": "2025-04-10T15:30:00"
      }
    ],
    "pageable": { "pageNumber": 0, "pageSize": 20, "offset": 0, "paged": true, "unpaged": false },
    "totalElements": 87,
    "totalPages": 5,
    "number": 0,
    "size": 20,
    "first": true,
    "last": false,
    "numberOfElements": 1,
    "empty": false
  }
}
```

> 클라이언트는 `data.content`, `data.totalElements`, `data.number` 사용.

---

### 2.3 `GET /api/v1/routes/{routeId}` — 루트 상세

루트 상세 정보 + 포함된 스팟 목록(스팟 상세정보 포함)을 반환합니다.

- `PRIVATE` 루트는 작성자 본인만 조회 가능, 타인 접근 시 `403 ROUTE_FORBIDDEN`
- `PUBLIC` 루트는 누구나 조회 가능
- `spots[]` 는 `SpotRepository` 배치 조회로 스팟 정보를 조인 (#31). 스팟이 삭제되어 없으면 `name` 이하 필드는 `null`

| 경로 파라미터 | 타입 | 설명 |
|------|------|------|
| `routeId` | number | 조회할 루트 ID |

응답:

```json
{
  "statusCode": 200,
  "message": "루트 상세 조회 성공",
  "data": {
    "routeId": 1,
    "userId": 42,
    "name": "경복궁 궁궐 투어",
    "description": null,
    "visibility": "PRIVATE",
    "spotCount": 3,
    "spots": [
      {
        "routeSpotId": 10,
        "spotId": 101,
        "sequenceOrder": 1,
        "name": "경복궁",
        "category": "K_HERITAGE",
        "address": "서울 종로구 사직로 161",
        "latitude": 37.57961700,
        "longitude": 126.97704100,
        "thumbnailUrl": "https://.../gyeongbokgung.jpg"
      }
    ],
    "createdAt": "2025-04-01T10:00:00",
    "updatedAt": "2025-04-10T15:30:00"
  }
}
```

`spots[]` 요소:

| 필드 | 타입 | 원천 |
|------|------|------|
| `routeSpotId` | number | `route_spots.id` |
| `spotId` | number | `route_spots.spot_id` |
| `sequenceOrder` | number | `route_spots.sequence_order` |
| `name` | string | `Spot.name` |
| `category` | enum | `Spot.category` — `K_DRAMA` / `K_POP` / `K_HERITAGE` / `K_MOVIE` |
| `address` | string \| null | `Spot.address` |
| `latitude` / `longitude` | number | `Spot.latitude` / `Spot.longitude` |
| `thumbnailUrl` | string \| null | `Spot.thumbnailUrl` |

> `kContentTitle`(대표 K-콘텐츠)은 content 도메인 연계가 필요해 후속 과제(10장).

---

### 2.4 `POST /api/v1/routes` — 루트 생성

요청 바디:

```json
{
  "name": "경복궁 궁궐 투어",
  "description": null,
  "visibility": "PRIVATE",
  "spots": [
    { "spotId": 101, "sequenceOrder": 1 },
    { "spotId": 105, "sequenceOrder": 2 }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | Y | 최대 50자 |
| `description` | string | N | 최대 512자 |
| `visibility` | enum | Y | `PUBLIC` / `PRIVATE` |
| `spots` | array | N | 생략 시 빈 루트 |
| `spots[].spotId` | number | Y | 추가할 스팟 ID |
| `spots[].sequenceOrder` | integer | Y | 순서 (1부터) |

응답 (201) — 상세 조회(2.3)와 동일한 `RouteResponse` 형태:

```json
{
  "statusCode": 201,
  "message": "루트 생성 성공",
  "data": {
    "routeId": 1,
    "userId": 42,
    "name": "경복궁 궁궐 투어",
    "description": null,
    "visibility": "PRIVATE",
    "spotCount": 2,
    "spots": [ ... ],
    "createdAt": "2025-04-13T12:00:00",
    "updatedAt": "2025-04-13T12:00:00"
  }
}
```

---

### 2.5 `PATCH /api/v1/routes/{routeId}` — 루트 수정

이름·설명·공개 여부를 수정합니다. 스팟 순서·추가·삭제는 3장 API 사용. 본인 루트만 수정 가능.

요청 바디 (모두 선택):

```json
{ "name": "수정된 루트 이름", "description": "업데이트된 설명", "visibility": "PRIVATE" }
```

응답 — `RouteResponse` 전체(스팟 포함):

```json
{
  "statusCode": 200,
  "message": "루트 수정 성공",
  "data": {
    "routeId": 1,
    "userId": 42,
    "name": "수정된 루트 이름",
    "description": "업데이트된 설명",
    "visibility": "PRIVATE",
    "spotCount": 1,
    "spots": [ ... ],
    "createdAt": "2025-04-01T10:00:00",
    "updatedAt": "2025-04-13T13:00:00"
  }
}
```

---

### 2.6 `DELETE /api/v1/routes/{routeId}` — 루트 삭제

루트 및 연결된 `route_spots` 를 함께 삭제합니다. 본인 루트만 삭제 가능.

응답 (`data: null`):

```json
{ "statusCode": 200, "message": "루트 삭제 성공", "data": null }
```

---

## 3. 루트 스팟 관리 API

### 3.1 `POST /api/v1/routes/{routeId}/spots` — 스팟 추가

- 같은 스팟이 이미 루트에 있으면 `409 ROUTE_SPOT_ALREADY_EXISTS`
- 현재 구현은 전달된 `sequenceOrder` 를 그대로 저장하며 뒤 스팟을 자동으로 밀지 않음

요청 바디:

```json
{ "spotId": 303, "sequenceOrder": 3 }
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `spotId` | number | Y | 추가할 스팟 ID |
| `sequenceOrder` | integer | Y | 삽입 위치 순서 (1 이상) |

응답 (201, `RouteSpotResponse`):

```json
{
  "statusCode": 201,
  "message": "스팟 추가 성공",
  "data": { "routeSpotId": 15, "spotId": 303, "sequenceOrder": 3 }
}
```

---

### 3.2 `PATCH /api/v1/routes/{routeId}/spots/reorder` — 순서 일괄 변경

루트 내 스팟 **전체**를 보내야 합니다. 개수가 다르면 `400 ROUTE_SPOT_ORDER_MISMATCH`,
존재하지 않는 `routeSpotId` 가 포함되면 `404 ROUTE_SPOT_NOT_FOUND`.

> 출발·도착 바꾸기: 별도 API 없이, 현재 `spots` 배열을 뒤집어 `sequenceOrder` 를 재부여해 전송.

요청 바디:

```json
{
  "spots": [
    { "routeSpotId": 10, "sequenceOrder": 1 },
    { "routeSpotId": 15, "sequenceOrder": 2 },
    { "routeSpotId": 12, "sequenceOrder": 3 }
  ]
}
```

응답 (`List<RouteSpotResponse>`):

```json
{
  "statusCode": 200,
  "message": "스팟 순서 변경 성공",
  "data": [
    { "routeSpotId": 10, "spotId": 101, "sequenceOrder": 1 },
    { "routeSpotId": 15, "spotId": 105, "sequenceOrder": 2 },
    { "routeSpotId": 12, "spotId": 103, "sequenceOrder": 3 }
  ]
}
```

---

### 3.3 `DELETE /api/v1/routes/{routeId}/spots/{routeSpotId}` — 스팟 제거

루트에서 특정 스팟만 제거합니다. `spots` 테이블은 건드리지 않고 `route_spots` 연결만 삭제.

| 경로 파라미터 | 타입 | 설명 |
|------|------|------|
| `routeId` | number | 루트 ID |
| `routeSpotId` | number | 제거할 `route_spots` 레코드 ID |

응답 (`data: null`):

```json
{ "statusCode": 200, "message": "스팟 제거 성공", "data": null }
```

---

## 4. 경로 계산(Directions) API

### 4.1 `POST /api/v1/routes/{routeId}/directions`

저장된 루트의 스팟 순서를 기준으로 이동수단별 경로(총 거리·시간, 구간별 소요시간, 폴리라인)를 계산합니다.

- 인접 스팟 쌍마다 Tmap 길찾기를 호출해 조립 (N개 스팟 → N-1 구간)
- `routeId + transportMode + 스팟구성 해시` 기준 **인메모리 캐시** (기본 300s TTL, `tmap.directions-cache-ttl-seconds`)
- `PRIVATE` 루트는 본인만, 타인은 `403 ROUTE_FORBIDDEN`
- 스팟 2개 미만이면 `400 DIRECTIONS_NOT_ENOUGH_SPOTS`
- Tmap 호출 실패 / `TMAP_APP_KEY` 미설정 시 `502 DIRECTIONS_PROVIDER_ERROR`

요청 헤더: `Content-Type: application/json`, `Authorization: Bearer {accessToken}`

요청 바디:

```json
{ "transportMode": "CAR" }
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `transportMode` | enum | Y | `WALK` / `TRANSIT` / `CAR` |

응답:

```json
{
  "statusCode": 200,
  "message": "경로 계산 성공",
  "data": {
    "routeId": 1,
    "transportMode": "CAR",
    "total": {
      "distanceMeters": 12400,
      "durationSeconds": 9000,
      "distanceText": "12.4km",
      "durationText": "약 2시간 30분"
    },
    "segments": [
      {
        "fromRouteSpotId": 10,
        "toRouteSpotId": 15,
        "fromSpotId": 101,
        "toSpotId": 105,
        "sequenceOrder": 1,
        "distanceMeters": 900,
        "durationSeconds": 900,
        "durationText": "자동차 15분",
        "geometry": {
          "type": "LineString",
          "coordinates": [[126.977041, 37.579617], [126.980, 37.581], [126.983746, 37.582604]]
        }
      }
    ],
    "geometry": {
      "type": "LineString",
      "coordinates": [[126.977041, 37.579617], [126.983746, 37.582604], [126.9766, 37.572]]
    },
    "calculatedAt": "2026-09-08T12:00:00"
  }
}
```

응답 필드:

| 필드 | 타입 | 설명 | 클라이언트 사용처 |
|------|------|------|--------------------|
| `total.distanceText` / `durationText` | string | "12.4km" / "약 2시간 30분" | `PlanSheet` 요약 카드 |
| `total.distanceMeters` / `durationSeconds` | number | 원본 수치 | 계산·정렬용 |
| `segments[]` | array | i번째 스팟 → i+1번째 스팟 구간 | `NavOverlay`, 구간 뱃지 |
| `segments[].durationText` | string | `"도보 42분"` / `"대중교통 15분"` / `"자동차 9분"` (이동수단 라벨 + N분) | `travelToNext(index)` 대체 |
| `segments[].geometry` | GeoJSON `LineString` | 구간 폴리라인 (`[경도, 위도]`) | `RouteMap` 구간 강조 |
| `geometry` | GeoJSON `LineString` | 전체 경로 폴리라인 | `RouteMap` polyline |
| `calculatedAt` | datetime | 계산 시각 | — |

**설계 메모**

- 좌표 순서는 GeoJSON 규격대로 **`[경도, 위도]`**. 클라이언트에서 `[lat, lng]` 로 뒤집어 사용.
- `WALK` → Tmap 보행자 경로안내(`/tmap/routes/pedestrian`), `CAR` → 자동차 경로안내(`/tmap/routes`), `TRANSIT` → 대중교통 경로안내(`/transit/routes`).
- `durationText` 는 서버에서 이동수단 라벨까지 완성해 내려줌 → 클라이언트 분기 불필요.

### 4.2 이동수단 Enum (`TransportMode`)

| 값 | 라벨 | 비고 |
|----|------|------|
| `WALK` | 도보 | |
| `TRANSIT` | 대중교통 | **Tmap 대중교통 API 승인 대기 중** — 승인 전에는 502 |
| `CAR` | 자동차 | |

클라이언트 `TransportMode`(`"walk" | "transit" | "car"`) ↔ 대문자 enum 매핑.

### 4.3 환경변수

| 키 | 설명 | 기본값 |
|----|------|--------|
| `TMAP_APP_KEY` | Tmap 오픈API AppKey (필수) | (없음 → 502) |
| `TMAP_BASE_URL` | Tmap API 베이스 URL | `https://apis.openapi.sk.com` |
| `TMAP_DIRECTIONS_CACHE_TTL_SECONDS` | 경로 계산 캐시 TTL(초) | `300` |

---

## 5. 클라이언트 루트 탭 매핑

`src/pages/route/` — 하나의 탭에서 3가지 모드로 전환.

| 모드 | 설명 | 주요 컴포넌트 |
|------|------|----------------|
| `empty` | 스팟이 하나도 없는 빈 상태 | 안내 카드 + "스팟 탐색하러 가기" |
| `plan` | 루트 편집 — 지도 + 하단 시트 | `RouteMap`, `PlanSheet`, `SpotPicker` |
| `nav` | 경로 탐색 — 지도 위 경로선 + 현재 스팟 안내 | `RouteMap`, `NavOverlay` |

### 5.1 화면 요소 → API

| # | 클라이언트 동작 | 엔드포인트 |
|---|------------------|------------|
| 1 | 탭 진입 시 루트 상세 불러오기 | `GET /api/v1/routes/{routeId}` (2.3) |
| 2 | 내 루트 목록에서 선택 | `GET /api/v1/routes` (2.1) |
| 3 | 빈 상태에서 새 루트 만들기 | `POST /api/v1/routes` (2.4, `spots` 생략) |
| 4 | 스팟 추가 시트 — 후보 목록 | `GET /api/v1/spots` (`category`, `regionId` 필터) |
| 5 | 루트에 스팟 추가 | `POST /api/v1/routes/{routeId}/spots` (3.1) |
| 6 | 스팟 개별 삭제 | `DELETE /api/v1/routes/{routeId}/spots/{routeSpotId}` (3.3) |
| 7 | 드래그 순서 변경 / 출발·도착 바꾸기 | `PATCH /api/v1/routes/{routeId}/spots/reorder` (3.2) |
| 8 | "루트 비우기" | `DELETE /api/v1/routes/{routeId}` (2.6) |
| 9 | "저장" (이름 등 수정) | `PATCH /api/v1/routes/{routeId}` (2.5) |
| 10 | 요약 카드 + 구간 소요시간 + 지도 경로선 | `POST /api/v1/routes/{routeId}/directions` (4.1) |
| 11 | 이동수단 변경 시 재계산 | 10번 재호출 (`transportMode` 변경) |
| 12 | 루트 공유 링크 | 미정 (10장) |
| 13 | 추천 루트(둘러보기) | `GET /api/v1/routes/public` (2.2) |

### 5.2 클라이언트 mock ↔ API

| 클라이언트 (현재 mock/local) | 대체 API |
|------------------------------|----------|
| `localStorage["wavey.route.ids"]` | `GET/POST/PATCH /api/v1/routes...` 서버 영속화 |
| `spots` mock 의 `coord`, `name`, `image` | 2.3 응답의 `latitude/longitude/name/thumbnailUrl` |
| `travelData` 하드코딩("도보 42분" 등) | 4.1 `segments[].durationText` |
| 요약 카드 `약 2시간 30분`, `12.4km` | 4.1 `total.durationText`, `total.distanceText` |
| `RouteMap` 데코용 경로선 | 4.1 `geometry` (GeoJSON LineString) |
| `SpotPicker` 후보 목록 | `GET /api/v1/spots` (+ `existingIds` 는 클라 필터) |

---

## 6. 이동수단 라벨 참고

| enum | 라벨 | `segments[].durationText` 예시 |
|------|------|-------------------------------|
| `WALK` | 도보 | `도보 42분` |
| `TRANSIT` | 대중교통 | `대중교통 15분` |
| `CAR` | 자동차 | `자동차 9분` |

---

## 7. 에러 코드 (실제 `ErrorCode` 기준)

| HTTP | `error.code` | enum 상수 | 설명 |
|------|--------------|-----------|------|
| `400` | `COMMON_INVALID_PARAMETER` | `COMMON_INVALID_PARAMETER` | 요청 파라미터 오류 |
| `400` | `ROUTE_SPOT_ORDER_MISMATCH` | `ROUTE_SPOT_ORDER_MISMATCH` | 재정렬 요청 스팟 개수 불일치 |
| `400` | `DIRECTIONS_NOT_ENOUGH_SPOTS` | `DIRECTIONS_NOT_ENOUGH_SPOTS` | 경로 계산에 스팟 2개 이상 필요 |
| `400` | `DIRECTIONS_UNSUPPORTED_MODE` | `DIRECTIONS_UNSUPPORTED_MODE` | 지원하지 않는 이동수단 |
| `401` | `TOKEN_401` | `INVALID_TOKEN` | 유효하지 않은 토큰 |
| `401` | `TOKEN_402` | `EXPIRED_TOKEN` | 만료된 토큰 — Refresh 필요 |
| `403` | `AUTH_403` | `ACCESS_DENIED` | 접근 권한 부족 |
| `403` | `ROUTE_FORBIDDEN` | `ROUTE_FORBIDDEN` | 해당 루트 접근 권한 없음 |
| `404` | `ROUTE_NOT_FOUND` | `ROUTE_NOT_FOUND` | 존재하지 않는 루트 |
| `404` | `SPOT_NOT_FOUND` | `SPOT_NOT_FOUND` | 존재하지 않는 스팟 |
| `404` | `ROUTE_SPOT_NOT_FOUND` | `ROUTE_SPOT_NOT_FOUND` | 루트에 해당 스팟 없음 |
| `409` | `ROUTE_SPOT_ALREADY_EXISTS` | `ROUTE_SPOT_ALREADY_EXISTS` | 이미 루트에 있는 스팟 |
| `502` | `DIRECTIONS_PROVIDER_ERROR` | `DIRECTIONS_PROVIDER_ERROR` | 외부 경로 엔진 호출 실패 |
| `500` | `SERVER_ERROR` | `INTERNAL_SERVER_ERROR` | 서버 오류 |

---

## 8. 데이터 모델 요약

- `routes` — `id`, `user_id`, `name(≤50)`, `description(≤512)`, `visibility`, `created_at`, `updated_at`
- `route_spots` — `id`, `route_id(FK)`, `spot_id`, `sequence_order` / `Route` 와 `@OneToMany(cascade=ALL, orphanRemoval=true)`, `@OrderBy("sequenceOrder ASC")`
  - `RouteSpot` 은 `Spot` 과 연관관계 없이 `spot_id(Long)` 만 보유 → 상세 조회 시 `SpotRepository` 배치 조회로 조인
- `spots` — 별도 도메인. `name`, `category(K_DRAMA/K_POP/K_HERITAGE/K_MOVIE)`, `address`, `latitude`, `longitude`, `thumbnail_url`, `avg_rating` 등

---

## 9. 인증 / 접근 제어

| 엔드포인트 | 인증 |
|-----------|------|
| `GET /api/v1/routes/public` | 불필요 (`permitAll`, #28) |
| 그 외 `/api/v1/routes/**` | 필수 (`Authorization: Bearer`) |

- 루트 상세·경로 계산: `PRIVATE` 루트는 소유자만. `PUBLIC` 루트는 조회/계산 모두 허용.
- 루트 수정·삭제·스팟 관리: 소유자만.

---

## 10. 미구현 / 후속 과제

- [ ] `GET /api/v1/routes` 페이징 (현재 `List` 전체 반환)
- [ ] `GET /api/v1/routes/public` `regionId` 지역 필터
- [ ] 루트 상세 `spots[].kContentTitle` (content 도메인 대표 K-콘텐츠 연계)
- [ ] `POST /api/v1/routes/{routeId}/spots` 삽입 시 이후 스팟 `sequenceOrder` 자동 재정렬
- [ ] `TRANSIT` — Tmap 대중교통 API 승인 후 활성화 (승인 시 AppKey 공유 여부 확인)
- [ ] `POST /api/v1/routes/directions` — 저장 없이(빈 상태) 즉석 계산 버전
- [ ] `GET /api/v1/spots` `excludeRouteId` 쿼리 파라미터
- [ ] 루트 공유 정책 확정 (공개 전환 후 링크 vs 별도 공유 토큰)
- [ ] Directions 캐시 — 규모에 따라 Caffeine / `@Cacheable` 전환 검토
