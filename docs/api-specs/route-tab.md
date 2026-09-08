# 루트 탭(Route Tab) API 명세서 — 클라이언트 관점

> 클라이언트 `루트` 탭 구현에 필요한 API를 화면 흐름 기준으로 정리한 문서입니다.
> 루트 CRUD·스팟 관리 상세 규격은 [`routes.md`](./routes.md) 를 따릅니다.
>
> **이 문서의 응답 JSON은 "현재 서버에 실제 구현된 형태"를 기준으로 작성되었습니다.**
> 아직 서버에 없는 부분은 `❌ 미구현` 으로 표시하며, 4·5장에 보강/신규 스펙을 별도로 둡니다.

---

## 1. 화면 구성

`src/pages/route/` — 하나의 탭에서 3가지 모드로 전환됩니다.

| 모드 | 설명 | 주요 컴포넌트 |
|------|------|----------------|
| `empty` | 루트에 스팟이 하나도 없는 빈 상태 | 안내 카드 + "스팟 탐색하러 가기" |
| `plan` | 루트 편집 — 지도 + 하단 시트(스팟 리스트/이동수단/요약) | `RouteMap`, `PlanSheet`, `SpotPicker` |
| `nav` | 경로 탐색 — 지도 위 경로선 + 현재 스팟 안내 | `RouteMap`, `NavOverlay` |

- 클라이언트는 **현재 편집 중인 루트 1개**만 다룹니다. (현재는 `localStorage["wavey.route.ids"]` 로 임시 저장)
- "더보기" 메뉴: 출발·도착 바꾸기 / 루트 공유 / 루트 비우기
- 하단 시트에서 스팟을 **드래그로 순서 변경**, `X`로 개별 삭제, "스팟 추가"로 `SpotPicker` 열기
- 이동수단: **도보 / 대중교통 / 자동차** 3종
- 요약 카드: `N개 스팟 · 약 2시간 30분 · 12.4km`
- `nav` 모드: 구간별 소요시간("지하철 15분"), 지도 경로선, 현재/다음 스팟 강조, "현재 위치로 이동"(GPS)

---

## 2. 화면 요소 → API 매핑

| # | 클라이언트 동작 | 메서드 · 엔드포인트 | 상태 |
|---|------------------|---------------------|------|
| 1 | 탭 진입 시 루트 상세 불러오기 | `GET /api/v1/routes/{routeId}` | ✅ 존재 · ⚠️ 스팟 상세필드 보강 필요 (4.1) |
| 2 | 내 루트 목록에서 선택 | `GET /api/v1/routes` | ✅ 존재 (페이징 없음, `data`가 배열) |
| 3 | 빈 상태에서 새 루트 만들기 | `POST /api/v1/routes` (`spots` 생략 가능) | ✅ 존재 |
| 4 | 스팟 추가 시트 — 후보 목록 | `GET /api/v1/spots` (`category`, `regionId` 필터) | ✅ 존재 (`excludeRouteId` 없음 → 클라 필터) |
| 5 | 루트에 스팟 추가 | `POST /api/v1/routes/{routeId}/spots` | ✅ 존재 |
| 6 | 스팟 개별 삭제 | `DELETE /api/v1/routes/{routeId}/spots/{routeSpotId}` | ✅ 존재 |
| 7 | 드래그 순서 변경 / 출발·도착 바꾸기 | `PATCH /api/v1/routes/{routeId}/spots/reorder` | ✅ 존재 (전체 배열 전송) |
| 8 | "루트 비우기" | `DELETE /api/v1/routes/{routeId}` | ✅ 존재 |
| 9 | "저장" (이름 등 수정) | `PATCH /api/v1/routes/{routeId}` | ✅ 존재 |
| 10 | 요약 카드(총 거리·시간) + 구간별 소요시간 + 지도 경로선 | `POST /api/v1/routes/{routeId}/directions` | ❌ **미구현 (5장)** |
| 11 | 이동수단 변경 시 재계산 | 위 10번 재호출 (`transportMode` 변경) | ❌ **미구현 (5장)** |
| 12 | 루트 공유 링크 | 미정 | ⚠️ 정책 확정 필요 |
| 13 | 추천 루트(둘러보기) | `GET /api/v1/routes/public` | ✅ 존재 · ⚠️ 현재 **인증 필요**, 응답이 Spring `Page` 원형 (4.3) |

---

## 3. 공통

### 3.1 응답 봉투 (현재 서버 구현)

```json
{
  "statusCode": 200,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { }
}
```

- `error` 필드는 `@JsonInclude(NON_NULL)` 설정으로 **성공 응답에서는 키 자체가 생략**됩니다. (실패 시에만 `error` 객체가 포함되고 `data`/`message`가 생략)
- 인증: 루트 탭의 모든 쓰기 API는 `Authorization: Bearer {accessToken}` 필요.
- 좌표: `latitude` / `longitude` (WGS84). `Spot` 엔티티는 위도 `DECIMAL(10,8)`, 경도 `DECIMAL(11,8)` — 소수점 8자리.
- 순서: `sequenceOrder` 는 **1부터** 시작하는 정수.

### 3.2 실패 응답 예시

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

## 4. 기존 API — 현재 응답 & 보강 사항

### 4.1 `GET /api/v1/routes/{routeId}` — 루트 상세

**현재 서버 응답 (`RouteResponse`):**

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
    "spots": [
      { "routeSpotId": 10, "spotId": 101, "sequenceOrder": 1 },
      { "routeSpotId": 15, "spotId": 105, "sequenceOrder": 2 },
      { "routeSpotId": 12, "spotId": 103, "sequenceOrder": 3 }
    ],
    "createdAt": "2025-04-01T10:00:00",
    "updatedAt": "2025-04-10T15:30:00"
  }
}
```

- `PRIVATE` 루트는 작성자 본인만 조회 가능, 타인 접근 시 `403 ROUTE_FORBIDDEN`.
- `PUBLIC` 루트는 누구나 조회 가능.

**⚠️ 보강 필요 — `spots[]` 요소에 스팟 상세필드 추가**

현재 `RouteSpotResponse` 는 `routeSpotId`, `spotId`, `sequenceOrder` 만 반환합니다.
루트 탭은 지도 마커·리스트·내비 안내에 스팟 이름/좌표/썸네일/카테고리가 필요하므로,
클라이언트가 스팟마다 `GET /spots/{id}` 를 N번 호출하지 않도록 상세 조회 시 함께 내려주어야 합니다.

보강된 `spots[]` 요소 (제안):

```json
{
  "routeSpotId": 10,
  "spotId": 101,
  "sequenceOrder": 1,
  "name": "경복궁",
  "category": "K_HERITAGE",
  "address": "서울 종로구 사직로 161",
  "latitude": 37.57961700,
  "longitude": 126.97704100,
  "thumbnailUrl": "https://.../gyeongbokgung.jpg",
  "kContentTitle": "눈물의 여왕 촬영지"
}
```

| 추가 필드 | 타입 | 서버 원천 | 비고 |
|-----------|------|-----------|------|
| `name` | string | `Spot.name` | 그대로 조인 |
| `category` | enum(`SpotCategory`) | `Spot.category` | **값: `K_DRAMA` / `K_POP` / `K_HERITAGE` / `K_MOVIE`** (`TOUR` 아님) |
| `address` | string \| null | `Spot.address` | nullable |
| `latitude` / `longitude` | number | `Spot.latitude/longitude` | `DECIMAL(x,8)` |
| `thumbnailUrl` | string \| null | `Spot.thumbnailUrl` | nullable |
| `kContentTitle` | string \| null | **없음 — 신규 매핑 필요** | content 도메인 별도, 대표 콘텐츠 선정 로직 필요 |

> 구현 메모: `RouteSpot` 엔티티에는 `Spot` 연관관계가 없고 `spotId(Long)` 만 보유.
> 상세필드를 채우려면 (a) `RouteSpot` ↔ `Spot` 연관 추가, 또는 (b) 서비스에서 `spotId` 목록으로 `SpotRepository` 배치 조회 후 조립.

> `spotCount` 필드도 상세 응답에 추가 제안 (현재는 `RouteSummaryResponse` 에만 존재).

---

### 4.2 `GET /api/v1/routes` — 내 루트 목록

쿼리 파라미터: `visibility` (`PUBLIC` / `PRIVATE`, 선택). **페이징 파라미터 없음.**

**현재 서버 응답 (`List<RouteSummaryResponse>` — `data` 가 곧 배열):**

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

### 4.3 `GET /api/v1/routes/public` — 공개 루트

쿼리 파라미터: `page`(기본 0), `size`(기본 20). `regionId` 필터는 **미구현**.

> ⚠️ 현재 `SecurityConfig` permitAll 목록에 없어 **토큰 없이 호출 시 401**. "인증 없이 접근" 하려면 서버 설정 추가 필요.

**현재 서버 응답 (Spring `Page` 직렬화 원형):**

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

> 클라이언트는 `data.content`, `data.totalElements`, `data.number` 를 사용. (`routes` / `currentPage` 아님)

---

### 4.4 `POST /api/v1/routes` — 루트 생성

요청 바디:

```json
{
  "name": "경복궁 궁궐 투어",
  "description": null,
  "visibility": "PRIVATE",
  "spots": [
    { "spotId": 101, "sequenceOrder": 1 }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | Y | 최대 50자 |
| `description` | string | N | 최대 512자 |
| `visibility` | enum | Y | `PUBLIC` / `PRIVATE` |
| `spots` | array | N | 생략 시 빈 루트 |

**현재 서버 응답 (201, `RouteResponse` — 상세 조회와 동일 형태):**

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
    "spots": [],
    "createdAt": "2025-04-13T12:00:00",
    "updatedAt": "2025-04-13T12:00:00"
  }
}
```

---

### 4.5 `PATCH /api/v1/routes/{routeId}` — 루트 수정

요청 바디 (모두 선택):

```json
{ "name": "수정된 루트 이름", "description": "업데이트된 설명", "visibility": "PRIVATE" }
```

**현재 서버 응답 (`RouteResponse` 전체 — `spots` 포함):**

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
    "spots": [
      { "routeSpotId": 10, "spotId": 101, "sequenceOrder": 1 }
    ],
    "createdAt": "2025-04-01T10:00:00",
    "updatedAt": "2025-04-13T13:00:00"
  }
}
```

---

### 4.6 `DELETE /api/v1/routes/{routeId}` — 루트 삭제

**현재 서버 응답 (`data: null`):**

```json
{ "statusCode": 200, "message": "루트 삭제 성공", "data": null }
```

---

### 4.7 `POST /api/v1/routes/{routeId}/spots` — 스팟 추가

요청 바디:

```json
{ "spotId": 303, "sequenceOrder": 3 }
```

- 이미 해당 루트에 있는 스팟이면 `409 ROUTE_SPOT_ALREADY_EXISTS`.
- 현재 구현은 전달된 `sequenceOrder` 를 그대로 저장하며, 뒤 스팟을 자동으로 밀지 않음.

**현재 서버 응답 (201, `RouteSpotResponse`):**

```json
{
  "statusCode": 201,
  "message": "스팟 추가 성공",
  "data": { "routeSpotId": 15, "spotId": 303, "sequenceOrder": 3 }
}
```

---

### 4.8 `PATCH /api/v1/routes/{routeId}/spots/reorder` — 순서 일괄 변경

요청 바디 — **루트의 전체 스팟**을 보내야 함 (개수 불일치 시 `400 ROUTE_SPOT_ORDER_MISMATCH`):

```json
{
  "spots": [
    { "routeSpotId": 10, "sequenceOrder": 1 },
    { "routeSpotId": 15, "sequenceOrder": 2 },
    { "routeSpotId": 12, "sequenceOrder": 3 }
  ]
}
```

출발·도착 바꾸기: 현재 `spots` 배열을 뒤집어 `sequenceOrder` 재부여 후 전송.

**현재 서버 응답 (`List<RouteSpotResponse>`):**

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

### 4.9 `DELETE /api/v1/routes/{routeId}/spots/{routeSpotId}` — 스팟 제거

**현재 서버 응답 (`data: null`):**

```json
{ "statusCode": 200, "message": "스팟 제거 성공", "data": null }
```

---

### 4.10 `GET /api/v1/spots` — 스팟 후보 목록 (SpotPicker)

쿼리 파라미터: `category`(enum, 선택), `regionId`(long, 선택). 페이징 없음. `excludeRouteId` 미구현.

**현재 서버 응답 (`List<SpotListResponse>`):**

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

## 5. 신규 API — 경로 계산 (Directions) ❌ 미구현

루트 탭 요약 카드·구간 소요시간·지도 경로선의 유일한 소스입니다.
현재 클라이언트는 `travelData` 를 하드코딩(`src/pages/route/page.tsx`)하고 있으며, 이를 대체합니다.
**서버에 `directions` 관련 코드·`TransportMode` enum·외부 경로엔진 연동이 전혀 없습니다.**

### 5.1 `POST /api/v1/routes/{routeId}/directions`

저장된 루트의 스팟 순서 기준으로 이동수단별 경로를 계산합니다.

요청 바디:

```json
{ "transportMode": "TRANSIT" }
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `transportMode` | enum | Y | `WALK` / `TRANSIT` / `CAR` |

응답 (제안):

```json
{
  "statusCode": 200,
  "message": "경로 계산 성공",
  "data": {
    "routeId": 1,
    "transportMode": "TRANSIT",
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
        "durationText": "지하철 15분",
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
    "calculatedAt": "2026-09-07T12:00:00"
  }
}
```

| 필드 | 타입 | 클라이언트 사용처 |
|------|------|--------------------|
| `total.distanceText` / `durationText` | string | `PlanSheet` 요약 카드 |
| `total.distanceMeters` / `durationSeconds` | number | 정렬·계산용 원본 |
| `segments[]` | array | i → i+1 구간, `NavOverlay`·구간 뱃지 |
| `segments[].durationText` | string | "도보 42분" / "지하철 15분" — `travelToNext(index)` 대체 |
| `segments[].geometry` | GeoJSON `LineString` | `RouteMap` 구간 강조 (`[lng, lat]`) |
| `geometry` | GeoJSON `LineString` | `RouteMap` 전체 폴리라인 |

설계 메모:

- 좌표 순서는 GeoJSON 규격 **`[경도, 위도]`**. 클라이언트에서 `[lat, lng]` 로 뒤집어 Leaflet 전달.
- 스팟 2개 미만 → `400 DIRECTIONS_NOT_ENOUGH_SPOTS`.
- 지원하지 않는 이동수단 → `400 DIRECTIONS_UNSUPPORTED_MODE`.
- 외부 경로엔진(카카오/네이버/OSRM 등) 실패 → `502 DIRECTIONS_PROVIDER_ERROR`.
- 성능: `routeId + transportMode + 스팟구성 해시` 캐시 권장(수분 TTL).
- `durationText` 는 서버에서 이동수단 접두어까지 완성해 내려주면 클라이언트 분기 제거 가능.

### 5.2 (대안) 저장 없이 계산 — `POST /api/v1/routes/directions`

```json
{ "transportMode": "WALK", "spotIds": [101, 105, 103] }
```

응답 형태는 5.1 과 동일하되 `fromRouteSpotId` / `toRouteSpotId` 는 생략(또는 null).

### 5.3 이동수단 Enum (`TransportMode`, 신규)

| 값 | 클라이언트 라벨 | 비고 |
|----|-----------------|------|
| `WALK` | 도보 | |
| `TRANSIT` | 대중교통 | 지하철/버스 혼합, `durationText` 는 대표 수단으로 표기 |
| `CAR` | 자동차 | |

클라이언트 `TransportMode`(`"walk" | "transit" | "car"`) 와 매핑. 요청/응답은 대문자 enum.

---

## 6. 에러 코드 (실제 `ErrorCode` 기준)

| HTTP | 코드 (`ErrorCode.code`) | enum 상수 | 설명 |
|------|--------------------------|-----------|------|
| `400` | `COMMON_INVALID_PARAMETER` | `COMMON_INVALID_PARAMETER` | 요청 파라미터 오류 |
| `400` | `ROUTE_SPOT_ORDER_MISMATCH` | `ROUTE_SPOT_ORDER_MISMATCH` | 재정렬 요청 스팟 개수/ID 불일치 |
| `401` | `TOKEN_401` | `INVALID_TOKEN` | 유효하지 않은 토큰 |
| `401` | `TOKEN_402` | `EXPIRED_TOKEN` | 만료된 토큰 — Refresh 필요 |
| `403` | `AUTH_403` | `ACCESS_DENIED` | 접근 권한 부족 |
| `403` | `ROUTE_FORBIDDEN` | `ROUTE_FORBIDDEN` | 해당 루트 접근 권한 없음 |
| `404` | `ROUTE_NOT_FOUND` | `ROUTE_NOT_FOUND` | 존재하지 않는 루트 |
| `404` | `SPOT_NOT_FOUND` | `SPOT_NOT_FOUND` | 존재하지 않는 스팟 |
| `404` | `ROUTE_SPOT_NOT_FOUND` | `ROUTE_SPOT_NOT_FOUND` | 루트에 해당 스팟 없음 |
| `409` | `ROUTE_SPOT_ALREADY_EXISTS` | `ROUTE_SPOT_ALREADY_EXISTS` | 이미 루트에 있는 스팟 |
| `500` | `SERVER_ERROR` | `INTERNAL_SERVER_ERROR` | 서버 오류 |

**신규로 추가해야 하는 에러 코드 (Directions):**

| HTTP | 코드 | 설명 |
|------|------|------|
| `400` | `DIRECTIONS_NOT_ENOUGH_SPOTS` | 경로 계산에 스팟 2개 이상 필요 |
| `400` | `DIRECTIONS_UNSUPPORTED_MODE` | 지원하지 않는 이동수단 |
| `502` | `DIRECTIONS_PROVIDER_ERROR` | 외부 경로엔진 호출 실패 |

---

## 7. 클라이언트 mock ↔ API 대응표

| 클라이언트 (현재 mock/local) | 대체 API |
|------------------------------|----------|
| `localStorage["wavey.route.ids"]` | `GET/POST/PATCH /api/v1/routes...` 서버 영속화 |
| `spots` mock 의 `coord`, `name`, `image` | 4.1 보강 응답의 `latitude/longitude/name/thumbnailUrl` |
| `travelData` 하드코딩("도보 42분" 등) | 5.1 `directions` → `segments[].durationText` |
| 요약 카드 `약 2시간 30분`, `12.4km` | 5.1 `directions` → `total.durationText`, `total.distanceText` |
| `RouteMap` 데코용 경로선 | 5.1 `directions` → `geometry` (GeoJSON LineString) |
| `SpotPicker` 후보 목록 | 4.10 `GET /api/v1/spots` (+ `existingIds` 는 클라 필터) |

---

## 8. 백엔드 작업 체크리스트

### 공통 응답 외 — 실제 구현 필요 (3덩어리)

- [ ] **(2) `GET /api/v1/routes/public` 인증 허용** — `SecurityConfig` permitAll 에 `/api/v1/routes/public` 추가. (응답 형태는 현행 `Page` 유지 or 명세 협의)
- [ ] **(3) `GET /api/v1/routes/{routeId}` 스팟 상세필드 조인**
  - [ ] `RouteSpot` ↔ `Spot` 연관 추가 또는 서비스 배치 조회
  - [ ] `spots[]` 에 `name`, `category`, `address`, `latitude`, `longitude`, `thumbnailUrl` 추가
  - [ ] `kContentTitle` — content 도메인에서 대표 K-콘텐츠 선정 로직 정의
  - [ ] `RouteResponse` 에 `spotCount` 추가
- [ ] **(4) Directions**
  - [ ] `TransportMode` enum (`WALK`, `TRANSIT`, `CAR`)
  - [ ] `POST /api/v1/routes/{routeId}/directions` (외부 경로엔진 연동 + 세그먼트/총합/폴리라인)
  - [ ] (선택) `POST /api/v1/routes/directions` 저장 전 계산 버전
  - [ ] 결과 캐시 (`routeId + mode + 스팟구성 해시`, 수분 TTL)
  - [ ] 에러코드 `DIRECTIONS_NOT_ENOUGH_SPOTS`, `DIRECTIONS_UNSUPPORTED_MODE`, `DIRECTIONS_PROVIDER_ERROR` 추가

### 문서/부가 (선택)

- [ ] (1) 본 문서·`routes.md` 의 `category` 예시 값을 실제 `SpotCategory`(`K_DRAMA`/`K_POP`/`K_HERITAGE`/`K_MOVIE`) 로 정정 — 완료
- [ ] (5) `routes.md` 에러코드 표를 실제 `ErrorCode` 상수/코드에 맞게 정정
- [ ] `GET /api/v1/spots` 에 `excludeRouteId` 쿼리 파라미터 (선택)
- [ ] 루트 공유 정책 확정 (공개 전환 후 링크 vs 별도 공유 토큰)
