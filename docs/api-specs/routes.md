# Route API 명세서

## 1. 설계 메모

- 모든 요청·응답 필드명은 `camelCase`를 기준으로 합니다.
- 인증이 필요한 모든 API는 `Authorization: Bearer {accessToken}` 헤더를 사용합니다.
- 루트는 `visibility` 필드로 공개 여부(`PRIVATE` / `PUBLIC`)를 관리합니다.
- 루트에 속한 스팟은 `sequenceOrder`로 순서를 관리하며, 순서 변경은 PATCH 요청으로 처리합니다.
- 외부 응답에서 내부 PK(`userId`, `routeId` 등)는 그대로 노출합니다.

### 1.1 공통 요청 헤더

- `Content-Type: application/json`
    - JSON 요청 바디가 있는 API에 사용합니다.
- `Authorization: Bearer {accessToken}`
    - 로그인 이후 인증이 필요한 API에 사용합니다.

---

## 2. 루트 CRUD API

### 2.1 `GET /api/v1/routes`

로그인한 사용자 본인의 루트 목록을 반환합니다.

- `visibility` 쿼리 파라미터로 공개 여부를 필터링할 수 있습니다.
- 미입력 시 전체 루트를 반환합니다.

요청 헤더:

- `Authorization: Bearer {accessToken}`

쿼리 파라미터:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `visibility` | string | N | `PUBLIC` / `PRIVATE` — 미입력 시 전체 반환 |
| `page` | integer | N | 페이지 번호 (기본값: 0) |
| `size` | integer | N | 페이지 크기 (기본값: 20) |

응답:

```json
{
  "statusCode": 200,
  "data": {
    "routes": [
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
    "totalElements": 12,
    "totalPages": 1,
    "currentPage": 0
  },
  "error": null
}
```

---

### 2.2 `GET /api/v1/routes/{routeId}`

루트 상세 정보 및 포함된 스팟 목록을 반환합니다.

- `PRIVATE` 루트는 작성자 본인만 조회 가능합니다. 타인이 접근할 경우 `403`을 반환합니다.

요청 헤더:

- `Authorization: Bearer {accessToken}`

경로 파라미터:

| 필드 | 타입 | 설명 |
|------|------|------|
| `routeId` | number | 조회할 루트 ID |

응답:

```json
{
  "statusCode": 200,
  "data": {
    "routeId": 1,
    "userId": 42,
    "name": "서울 야경 루트",
    "description": "한강과 남산을 잇는 야경 코스",
    "visibility": "PUBLIC",
    "spots": [
      {
        "routeSpotId": 10,
        "spotId": 101,
        "spotName": "남산서울타워",
        "address": "서울 용산구 남산공원길 105",
        "latitude": 37.5512,
        "longitude": 126.9882,
        "sequenceOrder": 1
      }
    ],
    "createdAt": "2025-04-01T10:00:00",
    "updatedAt": "2025-04-10T15:30:00"
  },
  "error": null
}
```

---

### 2.3 `POST /api/v1/routes`

새로운 루트를 생성합니다.

- 스팟은 생성 시 함께 추가하거나, 이후 스팟 추가 API를 통해 등록할 수 있습니다.

요청 헤더:

- `Content-Type: application/json`
- `Authorization: Bearer {accessToken}`

요청 바디:

```json
{
  "name": "서울 야경 루트",
  "description": "한강과 남산을 잇는 야경 코스",
  "visibility": "PUBLIC",
  "spots": [
    { "spotId": 101, "sequenceOrder": 1 },
    { "spotId": 202, "sequenceOrder": 2 }
  ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | Y | 루트 이름 (최대 50자) |
| `description` | string | N | 루트 설명 (최대 512자) |
| `visibility` | string | Y | `PUBLIC` / `PRIVATE` |
| `spots` | array | N | 초기 스팟 목록 (생략 시 빈 루트 생성) |
| `spots[].spotId` | number | Y | 추가할 스팟 ID |
| `spots[].sequenceOrder` | integer | Y | 루트 내 순서 (1부터 시작) |

응답:

```json
{
  "statusCode": 201,
  "data": {
    "routeId": 1,
    "name": "서울 야경 루트",
    "visibility": "PUBLIC",
    "spotCount": 2,
    "createdAt": "2025-04-13T12:00:00"
  },
  "error": null
}
```

---

### 2.4 `PATCH /api/v1/routes/{routeId}`

루트의 이름, 설명, 공개 여부를 수정합니다.

- 스팟 순서·추가·삭제는 별도 스팟 관리 API를 사용하세요.
- 본인의 루트만 수정 가능합니다.

요청 헤더:

- `Content-Type: application/json`
- `Authorization: Bearer {accessToken}`

요청 바디:

```json
{
  "name": "수정된 루트 이름",
  "description": "업데이트된 설명",
  "visibility": "PRIVATE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | N | 변경할 루트 이름 |
| `description` | string | N | 변경할 설명 |
| `visibility` | string | N | `PUBLIC` / `PRIVATE` |

응답:

```json
{
  "statusCode": 200,
  "data": {
    "routeId": 1,
    "name": "수정된 루트 이름",
    "description": "업데이트된 설명",
    "visibility": "PRIVATE",
    "updatedAt": "2025-04-13T13:00:00"
  },
  "error": null
}
```

---

### 2.5 `DELETE /api/v1/routes/{routeId}`

루트 및 연결된 `route_spots` 데이터를 함께 삭제합니다.

- 본인의 루트만 삭제 가능합니다.

요청 헤더:

- `Authorization: Bearer {accessToken}`

응답:

```json
{
  "statusCode": 200,
  "data": {
    "deleted": true
  },
  "error": null
}
```

---

## 3. 루트 스팟 관리 API

사용자가 원하는 스팟을 루트에 추가하거나 순서를 재배치하고 제거하는 기능입니다.

### 3.1 `POST /api/v1/routes/{routeId}/spots`

기존 루트에 스팟을 추가합니다.

- 같은 스팟이 이미 해당 루트에 존재하면 `409 ROUTE_SPOT_ALREADY_EXISTS`를 반환합니다.

요청 헤더:

- `Content-Type: application/json`
- `Authorization: Bearer {accessToken}`

요청 바디:

```json
{
  "spotId": 303,
  "sequenceOrder": 3
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `spotId` | number | Y | 추가할 스팟 ID |
| `sequenceOrder` | integer | Y | 삽입 위치 순서. 이후 스팟은 자동으로 밀림 |

응답:

```json
{
  "statusCode": 201,
  "data": {
    "routeSpotId": 15,
    "routeId": 1,
    "spotId": 303,
    "spotName": "경복궁",
    "sequenceOrder": 3
  },
  "error": null
}
```

---

### 3.2 `PATCH /api/v1/routes/{routeId}/spots/reorder`

루트 내 스팟 전체 순서를 한 번에 재정렬합니다.

- 요청 배열에 누락된 `routeSpotId`가 있으면 `400 ROUTE_SPOT_ORDER_MISMATCH`를 반환합니다.

요청 헤더:

- `Content-Type: application/json`
- `Authorization: Bearer {accessToken}`

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

응답:

```json
{
  "statusCode": 200,
  "data": {
    "reordered": true,
    "spots": [
      { "routeSpotId": 10, "spotName": "남산서울타워", "sequenceOrder": 1 },
      { "routeSpotId": 15, "spotName": "경복궁",       "sequenceOrder": 2 },
      { "routeSpotId": 12, "spotName": "한강공원",     "sequenceOrder": 3 }
    ]
  },
  "error": null
}
```

---

### 3.3 `DELETE /api/v1/routes/{routeId}/spots/{routeSpotId}`

루트에서 특정 스팟을 제거합니다.

- 스팟 자체(`spots` 테이블)는 삭제되지 않으며, 연결 데이터(`route_spots`)만 삭제됩니다.

요청 헤더:

- `Authorization: Bearer {accessToken}`

경로 파라미터:

| 필드 | 타입 | 설명 |
|------|------|------|
| `routeId` | number | 루트 ID |
| `routeSpotId` | number | 제거할 route_spots 레코드 ID |

응답:

```json
{
  "statusCode": 200,
  "data": {
    "deleted": true
  },
  "error": null
}
```

---

### 3.4 `GET /api/v1/routes/public`

모든 사용자의 공개(`PUBLIC`) 루트를 조회합니다.

- 인증 없이도 접근 가능합니다.

쿼리 파라미터:

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `regionId` | number | N | 지역 ID로 필터링 |
| `page` | integer | N | 페이지 번호 (기본값: 0) |
| `size` | integer | N | 페이지 크기 (기본값: 20) |

응답:

```json
{
  "statusCode": 200,
  "data": {
    "routes": [
      {
        "routeId": 1,
        "name": "서울 야경 루트",
        "description": "한강과 남산을 잇는 야경 코스",
        "spotCount": 4,
        "createdAt": "2025-04-01T10:00:00"
      }
    ],
    "totalElements": 87,
    "totalPages": 5,
    "currentPage": 0
  },
  "error": null
}
```

---

## 4. 에러 코드

| HTTP | 에러 코드 | 설명 |
|------|-----------|------|
| `400` | `COMMON_INVALID_PARAMETER` | 요청 파라미터가 잘못되었습니다. |
| `400` | `ROUTE_SPOT_ORDER_MISMATCH` | 재정렬 요청에 루트 스팟 ID가 누락되었습니다. |
| `401` | `AUTH_ACCESS_TOKEN_EXPIRED` | Access Token 만료 — Refresh 필요 |
| `403` | `ROUTE_FORBIDDEN` | 해당 루트에 접근 권한이 없습니다. |
| `404` | `ROUTE_NOT_FOUND` | 존재하지 않는 루트입니다. |
| `404` | `SPOT_NOT_FOUND` | 존재하지 않는 스팟입니다. |
| `404` | `ROUTE_SPOT_NOT_FOUND` | 루트에 해당 스팟이 없습니다. |
| `409` | `ROUTE_SPOT_ALREADY_EXISTS` | 이미 루트에 추가된 스팟입니다. |
| `500` | `INTERNAL_SERVER_ERROR` | 서버 오류 |