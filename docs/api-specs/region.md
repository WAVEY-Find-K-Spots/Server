# Region API 명세서

지역(Region) 마스터 데이터 도메인입니다. 스팟(Spot)이 지역에 속하며, 지역 필터링 등에 사용됩니다.

## 1. 설계 메모

- Base path: `/api/v1/regions`
- 조회는 로그인 유저만 가능(`isAuthenticated()`), 생성/수정/삭제는 ADMIN.

---

## 2. 엔드포인트

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/v1/regions` | 지역 생성 | ADMIN |
| GET | `/api/v1/regions` | 지역 목록 조회 | 로그인 유저 |
| GET | `/api/v1/regions/{regionId}` | 지역 상세 조회 | 로그인 유저 |
| PATCH | `/api/v1/regions/{regionId}` | 지역 수정 | ADMIN |
| DELETE | `/api/v1/regions/{regionId}` | 지역 삭제 | ADMIN |

### Request `RegionCreateRequest` / `RegionUpdateRequest`
```ts
{
  nameKo: string   // @NotBlank
  nameEn: string   // @NotBlank
}
```

### Response `RegionResponse`
```ts
{
  regionId: number
  nameKo: string
  nameEn: string
  createdAt: string
  updatedAt: string
}
```

---

## 3. 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| `REGION_NOT_FOUND` | 404 | 존재하지 않는 지역 |
| `REGION_ALREADY_EXISTS` | 409 | 이미 등록된 지역 (생성 시 중복) |
