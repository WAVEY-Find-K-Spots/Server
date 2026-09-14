# Spot API 명세서 — `GET /api/v1/spots`

`SpotPicker`(루트에 스팟 추가 시트) 연동에 필요한 스팟 목록 검색 API만 우선 정리한 문서입니다.
Spot 도메인 CRUD 등 나머지 엔드포인트는 별도로 채워질 예정입니다.
응답 예시는 **현재 서버 구현 형태**를 기준으로 작성했습니다.

---

## `GET /api/v1/spots` — 장소 목록 검색

키워드·지역·카테고리·장소 타입·평점·위치 반경으로 장소를 검색하고, 인기/평점/최신/거리순으로 정렬합니다.

- **인증 필요** (`Authorization: Bearer {accessToken}`, 컨트롤러 클래스 레벨 `@PreAuthorize("isAuthenticated()")`)
- 페이지 크기는 고정 **6** (요청으로 변경 불가, `page`만 조절)
- `latitude`/`longitude`는 반드시 함께 전달 (하나만 있으면 `400 COMMON_INVALID_PARAMETER`)
- `radiusMeters` 또는 `sort=DISTANCE` 사용 시 좌표가 없으면 `400 COMMON_INVALID_PARAMETER`

### 쿼리 파라미터

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `keyword` | string | N | - | 장소명(한/영) 또는 연관 콘텐츠 제목 부분 일치 검색. 최대 200자 |
| `regionId` | number | N | - | 지역 ID 필터 |
| `category` | enum | N | - | `K_DRAMA` / `K_POP` / `K_MOVIE` / `K_HERITAGE` |
| `placeType` | enum | N | - | `RESTAURANT` / `PLAYGROUND` / `CAFE` / `STAY` / `STATION` / `STORE` / `CVS` / `SHOP` / `OTHER` |
| `minRating` | number | N | - | 최소 평점 (0.0 ~ 5.0) |
| `latitude` | number | N | - | 위도 (33.0 ~ 38.7). `longitude`와 함께 전달 |
| `longitude` | number | N | - | 경도 (124.5 ~ 132.0). `latitude`와 함께 전달 |
| `radiusMeters` | number | N | - | 좌표 기준 반경(m), 1 ~ 100000. 좌표 필수 |
| `sort` | enum | N | `POPULAR` | `POPULAR`(저장수·리뷰수순) / `RATING`(평점순) / `LATEST`(최신순) / `DISTANCE`(거리순, 좌표 필수) |
| `page` | number | N | `0` | 0부터 시작 |
| `excludeRouteId` | number | N | - | 해당 루트에 이미 담긴 스팟을 결과에서 제외 (`SpotPicker`용, #70) |

### 응답 (200)

```json
{
  "statusCode": 200,
  "message": "장소 목록 조회 성공",
  "data": {
    "spots": [
      {
        "spotId": 101,
        "name": "경복궁",
        "description": "조선 왕조의 법궁",
        "category": "K_HERITAGE",
        "imageUrl": "https://.../thumb.jpg",
        "avgRating": 4.6,
        "reviewCount": 128,
        "saved": false,
        "distanceMeters": 320.5
      }
    ],
    "page": 0,
    "totalElements": 42,
    "totalPages": 7,
    "hasNext": true
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `spots[].spotId` | number | 스팟 ID |
| `spots[].name` | string | 요청 로케일 기준 장소명(한/영) |
| `spots[].description` | string | 요청 로케일 기준 설명. 없으면 `null` |
| `spots[].category` | enum | 장소 카테고리 |
| `spots[].imageUrl` | string | 대표 이미지 URL. 없으면 `null` |
| `spots[].avgRating` | number | 평균 평점 |
| `spots[].reviewCount` | number | 리뷰 수 |
| `spots[].saved` | boolean | 로그인 사용자의 저장 여부 — **현재 항상 `false`** (저장 스팟 조회 로직 미구현, `SpotSearchService.findSavedSpotIds`가 빈 Set 반환) |
| `spots[].distanceMeters` | number | 요청 좌표 기준 거리(m). 좌표 미전달 시 `null` |
| `page` | number | 현재 페이지(0-base) |
| `totalElements` | number | 전체 개수 |
| `totalPages` | number | 전체 페이지 수 |
| `hasNext` | boolean | 다음 페이지 존재 여부 |

> `routes.md`의 `Page` 직렬화 원형과 달리, 이 API는 자체 `SpotPageResponse` 형태(`spots`/`page`/`totalElements`/`totalPages`/`hasNext`)를 사용합니다. `content`가 아니라 `spots` 필드입니다.

### 에러 코드

| 코드 | HTTP | 상황 |
|------|------|------|
| `COMMON_INVALID_PARAMETER` | 400 | 좌표 중 하나만 전달, 또는 `radiusMeters`/`sort=DISTANCE` 사용 시 좌표 누락 |
| `AUTH_403` | 403 | 인증되지 않은 요청 |

### 알려진 제한사항

- `saved` 필드는 항상 `false` — 저장 스팟 여부를 실제로 반영하려면 서버 쪽 후속 작업 필요
- `keyword` 검색은 스팟명 외에 연관 콘텐츠 제목(`SpotContent`→`Content`)도 함께 검색됨
