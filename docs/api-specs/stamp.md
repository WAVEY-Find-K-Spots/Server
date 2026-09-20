
스탬프(Spot 방문 인증) + 배지(스탬프 조건 달성 시 수동 수령하는 컬렉션) 도메인입니다.
(PR #58 `feat/#50` 기준. 이슈 #50 관련.)

## 1. 설계 메모

- 모든 요청·응답 필드명은 `camelCase` 기준.
- 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더 사용.
- **스탬프는 lazy 생성**: Spot마다 `Stamp` 행이 미리 만들어져 있지 않고, 유저가 처음 `claim` 할 때 생성됨.
- **배지는 자동 지급되지 않음**: 스탬프 획득으로 조건이 충족돼도 `badges.claimable`에 노출만 되고, 유저가 직접 `POST /me/badges/{badgeId}/claim`을 호출해야 `user_badges`에 기록됨.
- 배지는 두 종류:
  - **집계형**: `regionId`/`category` 조건에 맞는 스팟을 몇 개 방문했는지로 진행도 계산 (`spotIds` 없음)
  - **세트형**: 지정된 `spotIds` 목록을 전부(또는 `requiredStamps`개) 방문했는지로 계산
- 응답 언어는 `language` 쿼리 파라미터(`ko`/`en`, 생략 시 기본 `ko`로 추정)로 제어.
- 배지 이미지는 범용 업로드 플로우 사용: `POST /api/v1/uploads/presigned-url` (`category: "BADGE"`) → 업로드 → `fileUrl`을 `imageUrl`로 저장. ([upload.md](./upload.md) 참고)

---

## 2. 스탬프 API (`/api/v1`)

### 2.1 `POST /api/v1/spots/{spotId}/stamp` — 스탬프 획득

현재 위치가 스팟 반경(**150m**) 안이면 스탬프를 획득합니다. 해당 Spot의 `Stamp` 행이 없으면 이때 생성(lazy)됩니다. 이미 획득한 경우 거리 검사 없이 `newlyAcquired=false`로 반환됩니다.

- 인증: 필요
- path: `spotId`
- query: `language` (`ko`/`en`, 선택)

요청 바디 `StampClaimRequest`:

```ts
{
  latitude: number    // @NotNull, -90~90
  longitude: number   // @NotNull, -180~180
}
```

응답 (`StampService.Claim`):

```json
{
  "statusCode": 200,
  "message": "스탬프 획득",
  "data": {
    "stamp": {
      "stampId": 1,
      "spotId": 10,
      "regionId": 1,
      "name": "경복궁",
      "imageUrl": "https://example.com/spots/10.png",
      "acquired": true,
      "acquiredAt": "2026-09-14T12:00:00"
    },
    "newlyAcquired": true,
    "badges": {
      "acquiredCount": 0,
      "claimableCount": 1,
      "inProgressCount": 1,
      "acquired": [],
      "claimable": [
        {
          "badgeId": 1,
          "name": "서울 탐험가",
          "description": "서울 스팟 5곳 방문",
          "imageUrl": "https://example.com/badges/1.png",
          "requiredStamps": 5,
          "progress": 5,
          "acquiredAt": null
        }
      ],
      "inProgress": [
        {
          "badgeId": 2,
          "name": "한강 러버",
          "description": "한강 스팟 3곳 방문",
          "imageUrl": "https://example.com/badges/2.png",
          "requiredStamps": 3,
          "progress": 1,
          "acquiredAt": null
        }
      ]
    }
  }
}
```

> `badges`는 이번 스탬프 획득으로 갱신된 배지 진행 현황 스냅샷 — `claimable`에 뜬 배지는 아직 `user_badges`에 기록 안 됨, 수령하려면 2.5(배지 수령) 호출 필요.

에러: 좌표 유효성 실패 또는 반경 밖이면 `400 STAMP_TOO_FAR`, 스팟/유저 없으면 `404`.

---

### 2.2 `GET /api/v1/me/stamps` — 스탬프북 조회

모든 Spot을 스탬프 후보로 페이지 조회합니다. 이름/이미지는 Spot에서 읽고, `acquired`는 내 획득 여부입니다.

- 인증: 필요
- query: `regionId`(선택), `language`(선택), `page`(기본 0), `size`(기본 20, 최대 100)

응답:

```json
{
  "statusCode": 200,
  "message": "스탬프북",
  "data": {
    "collectedCount": 3,
    "stamps": [
      {
        "stampId": null,
        "spotId": 10,
        "regionId": 1,
        "name": "경복궁",
        "imageUrl": "https://example.com/spots/10.png",
        "acquired": false,
        "acquiredAt": null
      }
    ],
    "page": 0,
    "totalElements": 57,
    "totalPages": 3,
    "hasNext": true
  }
}
```

> 미획득 스팟은 `stampId: null` (아직 `Stamp` 행이 생성 안 됨 — lazy 생성이라).

---

### 2.3 `GET /api/v1/me/stamps/{id}` — 스탬프 상세 조회

`stampId`로 단건 조회. 미획득 스탬프는 프론트에서 상세 진입을 막는 걸 전제로 하므로(정상 플로우에서는 claim 이후에만 호출), 존재하지 않는 `id`는 `404`.

응답:

```json
{
  "statusCode": 200,
  "message": "스탬프 상세",
  "data": {
    "stampId": 1,
    "spotId": 10,
    "regionId": 1,
    "name": "경복궁",
    "imageUrl": "https://example.com/spots/10.png",
    "acquired": true,
    "acquiredAt": "2026-09-14T12:00:00"
  }
}
```

---

## 3. 배지 API (유저용, `/api/v1/me/badges`)

### 3.1 `GET /api/v1/me/badges` — 내 배지함 조회

`acquired`(획득) / `claimable`(조건 충족·미수령) / `inProgress`(도전 중) 3섹션으로 분리.

- 인증: 필요
- query: `language`(선택)

응답:

```json
{
  "statusCode": 200,
  "message": "배지함",
  "data": {
    "acquiredCount": 1,
    "claimableCount": 1,
    "inProgressCount": 1,
    "acquired": [
      { "badgeId": 1, "name": "서울 탐험가", "description": "서울 스팟 5곳 방문", "imageUrl": "https://example.com/badges/1.png", "requiredStamps": 5, "progress": 5, "acquiredAt": "2026-09-14T12:00:00" }
    ],
    "claimable": [
      { "badgeId": 3, "name": "카페 마스터", "description": "카페 3곳 방문", "imageUrl": "https://example.com/badges/3.png", "requiredStamps": 3, "progress": 3, "acquiredAt": null }
    ],
    "inProgress": [
      { "badgeId": 2, "name": "한강 러버", "description": "한강 스팟 3곳 방문", "imageUrl": "https://example.com/badges/2.png", "requiredStamps": 3, "progress": 1, "acquiredAt": null }
    ]
  }
}
```

### 3.2 `POST /api/v1/me/badges/{badgeId}/claim` — 배지 수령

조건이 충족된 배지를 사용자가 직접 수령. 이미 보유한 경우 `newlyAcquired=false`로 멱등 반환. 진행도 미달이면 `400 BADGE_400_NOT_CLAIMABLE`.

응답:

```json
{
  "statusCode": 200,
  "message": "배지 수령",
  "data": {
    "badge": {
      "badgeId": 3,
      "name": "카페 마스터",
      "description": "카페 3곳 방문",
      "imageUrl": "https://example.com/badges/3.png",
      "requiredStamps": 3,
      "progress": 3,
      "acquiredAt": "2026-09-15T00:50:00"
    },
    "newlyAcquired": true
  }
}
```

---

## 4. 배지 관리자 API (`/api/v1/admin/badges`, ADMIN 전용)

| Method | Path | 설명 |
|---|---|---|
| POST | `/api/v1/admin/badges` | 배지 생성 (집계형/세트형) |
| GET | `/api/v1/admin/badges` | 배지 목록 조회 |
| GET | `/api/v1/admin/badges/{badgeId}` | 배지 단건 조회 |
| PATCH | `/api/v1/admin/badges/{badgeId}` | 배지 수정 (전달한 필드만 반영) |
| DELETE | `/api/v1/admin/badges/{badgeId}` | 배지 삭제 (`user_badges` 기록도 함께 삭제) |

### Request `BadgeCreateRequest`

```ts
{
  name: string            // @NotBlank, max 100
  nameEn?: string          // max 100
  description?: string     // max 500
  descriptionEn?: string   // max 500
  imageUrl?: string        // max 1000, uploads/presigned-url(category=BADGE)의 fileUrl
  requiredStamps: number   // @NotNull @Min(1) — 세트형이면 보통 spotIds.length
  regionId?: number        // 집계형 전용, @Positive
  category?: SpotCategory  // 집계형 전용
  spotIds?: number[]       // 있으면 세트형(이 스팟들만 카운트), 없으면 집계형
}
```

집계형 예시:
```json
{ "name": "서울 탐험가", "nameEn": "Seoul Explorer", "description": "서울 스팟 5곳 방문", "requiredStamps": 5, "regionId": 1, "category": null, "spotIds": null }
```

세트형 예시:
```json
{ "name": "궁궐 마스터", "nameEn": "Palace Master", "description": "서울 4대 궁궐 모두 방문", "requiredStamps": 4, "regionId": null, "category": null, "spotIds": [10, 11, 12, 13] }
```

### Request `BadgeUpdateRequest`

`BadgeCreateRequest`와 필드 구성은 비슷하나 전부 선택값(부분 수정)이고, 추가로:
- `clearRegionId: boolean` — `true`면 `regionId`를 `null`로 초기화
- `clearCategory: boolean` — `true`면 `category`를 `null`로 초기화
- `imageUrl: ""`(빈 문자열) — 이미지 제거
- `spotIds`: `null`이면 유지, `[]`면 세트 해제(집계형으로 전환), 값이 있으면 교체

### Response `BadgeAdminResponse`

```ts
{
  badgeId: number
  name: string
  nameEn: string | null
  description: string | null
  descriptionEn: string | null
  imageUrl: string | null
  requiredStamps: number
  regionId: number | null
  category: SpotCategory | null
  spotIds: number[]        // 비어있으면 집계형
  setType: boolean         // spotIds 존재 여부
  createdAt: string
  updatedAt: string
}
```

---

## 5. 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| `STAMP_TOO_FAR` | 400 | 스탬프 획득 가능 거리(150m) 밖 |
| `BADGE_404` (`BADGE_NOT_FOUND`) | 404 | 해당 배지를 찾을 수 없음 |
| `BADGE_400_NOT_CLAIMABLE` | 400 | 아직 배지 수령 조건 미충족 |
| `REGION_NOT_FOUND` | 404 | 배지 생성/수정 시 존재하지 않는 `regionId` |
| `SPOT_NOT_FOUND` | 404 | 배지 생성/수정 시 존재하지 않는 `spotId` (세트형) |
| `UPLOAD_INVALID_FILE_URL` | 400 | 배지 `imageUrl`이 본인(관리자) 업로드 경로가 아님 |

---

## 6. 미구현 / 후속 과제

- 배지 조건 알림(달성 시 푸시 등)은 이번 범위 아님
- Spot 삭제 시 `stamp`/`user_stamp`/`badge_spot` cascade 삭제는 서버에 구현되어 있음(PR #58 Test plan 참고) — 프론트에서 별도 처리 불필요
