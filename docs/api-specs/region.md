# Region API 명세서

지역(Region) 도메인의 API 규격입니다. 스팟이 참조하는 지역 정보를 관리합니다.
응답 예시는 **현재 서버 구현 형태**를 기준으로 작성했습니다.

## 구현 상태

| 범위 | 상태 |
|------|------|
| 지역 CRUD (`/api/v1/regions`) | 구현됨 |

> 미구현/후속 항목은 [8장](#8-미구현--후속-과제) 참고.

---

## 1. 설계 메모

- Base path: `/api/v1/regions`
- 공통 응답 봉투는 [routes.md](./routes.md#12-공통-응답-봉투) 와 동일
- `code` 는 지역 고유 코드로 **unique** (중복 시 `409 REGION_ALREADY_EXISTS`)
- 좌표는 선택값 (`latitude` `DECIMAL(10,8)`, `longitude` `DECIMAL(11,8)`)
- 인증: `/api/v1/regions/**` 전부 인증 필요. 역할(ADMIN) 제한 없음.
- 생성은 `201`, 그 외 성공은 `200` (`ResponseEntity` 사용)

---

## 2. `POST /api/v1/regions` — 지역 생성

요청 바디:

```json
{
  "name": "서울",
  "code": "SEOUL",
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | string | Y | 지역명 |
| `code` | string | Y | 지역 고유 코드 (unique) |
| `latitude` | number | N | -90 ~ 90 |
| `longitude` | number | N | -180 ~ 180 |

응답 (201, `RegionResponse`):

```json
{
  "statusCode": 201,
  "message": "지역 생성 성공",
  "data": {
    "regionId": 1,
    "name": "서울",
    "code": "SEOUL",
    "latitude": 37.56650000,
    "longitude": 126.97800000,
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
}
```

- 중복 `code` → `409 REGION_ALREADY_EXISTS`

---

## 3. `GET /api/v1/regions/{regionId}` — 지역 단건 조회

응답: `RegionResponse` (2장과 동일). 없으면 `404 REGION_NOT_FOUND`.

---

## 4. `GET /api/v1/regions` — 지역 목록 조회

전체 지역을 반환합니다 (페이징 없음).

```json
{
  "statusCode": 200,
  "message": "지역 목록 조회 성공",
  "data": [
    { "regionId": 1, "name": "서울", "code": "SEOUL", "latitude": 37.56650000, "longitude": 126.97800000, "createdAt": "...", "updatedAt": "..." }
  ]
}
```

---

## 5. `PATCH /api/v1/regions/{regionId}` — 수정 / `DELETE` — 삭제

### 5.1 수정

요청 바디 (모두 선택):

```json
{ "name": "서울특별시", "code": "SEOUL", "latitude": 37.5665, "longitude": 126.9780 }
```

응답: `RegionResponse`. 없으면 `404 REGION_NOT_FOUND`, 중복 `code` → `409 REGION_ALREADY_EXISTS`.

### 5.2 삭제

```json
{ "statusCode": 200, "message": "지역 삭제 성공", "data": null }
```

---

## 6. 에러 코드 (실제 `ErrorCode` 기준)

| HTTP | `error.code` | enum 상수 | 설명 |
|------|--------------|-----------|------|
| `400` | `COMMON_INVALID_PARAMETER` | `COMMON_INVALID_PARAMETER` | 요청 파라미터 오류 |
| `404` | `REGION_NOT_FOUND` | `REGION_NOT_FOUND` | 존재하지 않는 지역 |
| `409` | `REGION_ALREADY_EXISTS` | `REGION_ALREADY_EXISTS` | 이미 등록된 지역 코드 |

---

## 7. 데이터 모델

`regions` 테이블 (`Region` 엔티티, PK 컬럼 `region_id`):

| 컬럼 | 타입 | 제약 |
|------|------|------|
| `region_id` | bigint | PK |
| `name` | varchar(100) | not null |
| `code` | varchar(50) | not null, unique |
| `latitude` / `longitude` | decimal(10,8) / (11,8) | nullable |
| `created_at` / `updated_at` | timestamp | `BaseEntity` |

인덱스: `code`(unique), `name`

---

## 8. 미구현 / 후속 과제

- [ ] 지역 CRUD 권한 제한 (현재 인증된 누구나 생성/수정/삭제 가능 → `ADMIN` 제한 검토)
- [ ] 지역 삭제 시 참조 중인 `spots` 처리 정책 (현재 FK 제약 없음)
- [ ] `GET /api/v1/routes/public` 의 `regionId` 필터 연동 ([routes.md](./routes.md) 참고)
