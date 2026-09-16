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
  imageUrl: string
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
  imageUrl: string
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
  imageUrl: string
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

## 8. 스팟 이미지 백필 (관리자, `/api/v1/spots/sync`)

`imageUrl`이 비어있는 스팟에 사진을 채우는 관리자 도구. 두 소스를 순서대로 씀:

1. 관광공사 관광사진 갤러리(`galleryList1`) — 무료, 이름 정확 일치 기준 일회성 백필(SQL로 직접 실행, `docs/migration/V2026091601__*`, `V2026091602__*` 참고)
2. **Google Places API(New)** — 그래도 안 채워진 스팟 대상. 월별 무료 제공량(Photo 1,000장/월, Text Search 5,000건/월)을 넘지 않도록 자동 제한.

```
POST /api/v1/spots/sync/google-places/enrich-images?limit=50
Authorization: Bearer {accessToken}  (ADMIN)
```

- `limit`: 이번 호출에서 처리할 최대 스팟 수 (기본 50). 남은 월 예산보다 크게 줘도 예산만큼만 처리됨.
- 장소명(`nameKo`) + 주소(`addressKo`)로 Google Places Text Search → 대표 사진 1장 다운로드 → 우리 S3 버킷에 재업로드(구글 API 키가 공개 URL에 노출되지 않도록) → `imageUrl` 갱신.
- 무료 제공량은 **월 단위 리셋**(매월 1일 태평양시간 자정) — 하루 단위 아님.

응답 (`SpotPlacesEnrichResponse`):

```ts
{
  requested: number        // 이번에 시도한 스팟 수
  filled: number           // 실제로 imageUrl 채운 수
  skipped: number          // 검색 결과/사진 없음으로 건너뜀
  budgetBefore: number     // 호출 전 남은 이번 달 Photo 예산
  budgetAfter: number      // 호출 후 남은 예산
  stoppedByBudget: boolean // 예산 소진으로 중단됐는지
}
```

⚠️ 예산 카운터는 인메모리라 **배포 재시작 시 리셋됨**(기존 TourAPI 동기화 예산 서비스와 동일한 한계). 실제 Google Cloud Console의 사용량과는 별개이니, 정확한 잔여량은 GCP 콘솔에서도 교차 확인 권장.

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
