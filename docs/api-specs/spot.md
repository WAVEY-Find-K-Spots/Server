# Spot API 명세서

스팟(Spot, 장소) 도메인입니다. K-콘텐츠 관련 촬영지/장소 정보와 검색/근처 스팟 조회를 제공합니다.

## 1. 설계 메모

- Base path: `/api/v1/spots`
- 클래스 레벨 기본 권한은 `isAuthenticated()`이며, 생성/수정/삭제는 ADMIN으로 override됨.
- 좌표 범위(대한민국 기준): 위도 `33.0 ~ 38.7`, 경도 `124.5 ~ 132.0`.
- 조회 API에서 `@AuthenticationPrincipal User`는 nullable — 비로그인 유저도 조회 가능하지만 `saved`(찜 여부) 값은 항상 `false`로 내려감.
- 찜(저장) 기능: `POST/DELETE /api/v1/spots/{spotId}/save`로 토글, `saved_spots(user_id, spot_id)` 테이블로 관리. `GET /api/v1/spots?savedOnly=true`로 내가 찜한 스팟만 필터링 가능(기존 검색 API 재사용, 정렬/페이지네이션 그대로 적용됨).

---

## 2. 엔드포인트

| Method | Path | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/v1/spots` | 스팟 생성 | ADMIN |
| GET | `/api/v1/spots` | 스팟 검색/목록 (필터+페이징) | 로그인 유저 (비로그인도 조회는 가능, `saved`=false) |
| GET | `/api/v1/spots/{spotId}` | 스팟 상세 | 로그인 유저 |
| GET | `/api/v1/spots/{spotId}/nearby?radiusMeters={m}` | 특정 스팟 기준 근처 스팟 목록 | 로그인 유저 |
| PATCH | `/api/v1/spots/{spotId}` | 스팟 수정 (부분 수정) | ADMIN |
| DELETE | `/api/v1/spots/{spotId}` | 스팟 삭제 | ADMIN |
| POST | `/api/v1/spots/{spotId}/save` | 스팟 찜(저장), 멱등 | 로그인 유저 |
| DELETE | `/api/v1/spots/{spotId}/save` | 스팟 찜 해제, 멱등 | 로그인 유저 |

---

## 3. Request `SpotCreateRequest`

```ts
{
  regionId: number          // @NotNull @Positive
  nameKo: string            // @NotBlank, max 255
  nameEn?: string           // max 255
  category: SpotCategory    // @NotNull
  placeType: PlaceType      // @NotNull
  descriptionKo?: string
  descriptionEn?: string
  openingHours?: string     // max 500
  breakTime?: string        // max 100
  closedDaysKo?: string     // max 255
  closedDaysEn?: string     // max 255
  tel?: string              // max 50
  addressKo: string         // @NotBlank, max 500
  addressEn?: string        // max 500
  transportInfoKo?: string  // max 500
  transportInfoEn?: string  // max 500
  latitude: number          // @NotNull, 33.0~38.7
  longitude: number         // @NotNull, 124.5~132.0
  imageUrl?: string         // max 500
}
```

## 4. Request `SpotUpdateRequest`

`SpotCreateRequest`와 동일한 필드 구성이지만 **전부 선택값** — 보낸 필드만 부분 수정됨.
(`nameKo`는 값이 있으면 공백 불가, `regionId`/좌표는 값이 있으면 범위 검증만 적용, `@NotNull`/`@NotBlank` 없음)

## 5. Request `SpotSearchRequest` (쿼리 파라미터, GET `/spots`)

| 파라미터 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `keyword` | string | max 200 | 이름/설명 검색어 |
| `regionId` | number | `@Positive` | 지역 필터 |
| `category` | `SpotCategory` | | 카테고리 필터 |
| `placeType` | `PlaceType` | | 장소 유형 필터 |
| `minRating` | number | 0.0~5.0 | 최소 평점 |
| `latitude`, `longitude` | number | 좌표 범위 동일 | 좌표 기반(반경) 검색용 |
| `radiusMeters` | number | 1.0~100000.0 | 반경(m) |
| `sort` | `SortBy` | 기본값 `POPULAR` | 정렬 기준 |
| `page` | number | `@Min(0)`, 기본값 `0` | 페이지 번호 |
| `excludeRouteId` | number | `@Positive` | 특정 루트에 이미 포함된 스팟 제외 (루트 편집 시 "스팟 추가" 피커에서 사용) |
| `savedOnly` | boolean | | `true`면 로그인 유저가 찜한 스팟만 반환 (마이페이지 "저장한 스팟" 목록용). 비로그인 상태로 `true` 주면 빈 목록. |

---

## 6. Response

### `SpotResponse` (상세)
```ts
{
  spotId: number
  name: string
  description: string
  category: SpotCategory
  imageUrl: string | null
  avgRating: number
  reviewCount: number
  saved: boolean
  openingHours: string
  breakTime: string
  closedDays: string
  address: string
  transportInfo: string
  tel: string
  latitude: number
  longitude: number
}
```

### `SpotListResponse` (검색 목록 아이템)
```ts
{
  spotId: number
  name: string
  description: string
  category: SpotCategory
  imageUrl: string | null
  avgRating: number
  reviewCount: number
  saved: boolean
  distanceMeters: number | null   // 좌표 검색 시에만 값 존재
}
```

### `SpotPageResponse` (검색 목록 래퍼)
```ts
{
  spots: SpotListResponse[]
  page: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}
```

### `SpotNearbyResponse` (근처 스팟)
```ts
{
  spotId: number
  name: string
  description: string
  address: string
  imageUrl: string | null
  avgRating: number
  distanceMeters: number
}
```

### `SpotSaveResponse` (찜 토글 결과, `POST`/`DELETE .../save`)
```ts
{
  spotId: number
  saved: boolean       // 토글 후 최종 상태
  savedCount: number   // 해당 스팟을 저장한 전체 유저 수
}
```

---

## 7. Enum

- `SpotCategory`: `K_DRAMA`, `K_POP`, `K_MOVIE`, `K_HERITAGE`
- `PlaceType`: `RESTAURANT`, `PLAYGROUND`, `CAFE`, `STAY`, `STATION`, `STORE`, `CVS`, `SHOP`, `OTHER`
- `SortBy`: `POPULAR`, `RATING`, `LATEST`, `DISTANCE`

---

## 8. 스팟 이미지 백필 (관리자)

`imageUrl`이 비어있는 스팟에 사진을 채우는 관리자 도구.

- **관광공사 관광사진 갤러리(`galleryList1`)** — 무료, 이름 정확 일치 기준 일회성 백필(SQL로 직접 실행, `docs/migration/V2026091601__*`, `V2026091602__*` 참고). 1,372건 완료.
- **~~Google Places API~~ — 사용 금지.** 정책상 사진(Photos) 콘텐츠는 캐싱/저장이 전면 금지되어([공식 정책](https://developers.google.com/maps/documentation/places/web-service/policies)) 시도했던 백필(#106/PR #107)을 되돌렸다(#108). `place_id`만 무기한, 좌표는 30일 예외가 있지만 **사진은 예외 없음**.
- **~~Wikimedia Commons~~ — 시도했으나 접음(#110/PR #111, #114).** K_HERITAGE 중 이미지 없는 스팟이 4건뿐이라 채울 대상 자체가 거의 없었고, 그마저도 이름을 줄여 재검색하면 무관한 이미지(예: "강경역사관" → "강경역" 기차역 사진)가 매칭되는 등 정확도가 낮아 비용 대비 효과가 없다고 판단해 코드까지 되돌렸다. K_DRAMA/K_POP/K_MOVIE는 흔한 일반명사 상호명이 많아(예: "카페", "퀸즈파크") 시도 자체가 더 위험했다(#112에서 확인).
- 남은 스팟(대부분 K_DRAMA/K_POP/K_MOVIE 촬영지)의 이미지는 프론트에서 `imageUrl`이 `null`일 때 `category`/`placeType` 기반 기본 일러스트로 대체하는 것을 권장.

---

## 9. 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| `SPOT_NOT_FOUND` | 404 | 존재하지 않는 장소 |
| `REGION_NOT_FOUND` | 404 | 생성/수정 시 존재하지 않는 `regionId` 지정 |
| `COMMON_INVALID_PARAMETER` | 400 | 검색 파라미터 조합 오류 (예: 위도만 주고 경도 누락) |
| `SPOT_EXTERNAL_DATA_ALREADY_EXISTS` | 409 | (외부 데이터 연동 관련, 이 컨트롤러 밖 기능) |
| `SPOT_INVALID_MAP_BOUNDS` | 400 | (외부 데이터 연동 관련) |
| `SPOT_EXTERNAL_API_KEY_MISSING` / `SPOT_EXTERNAL_API_REQUEST_FAILED` | 400/502 | (외부 데이터 연동 관련) |
