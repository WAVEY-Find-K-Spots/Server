# WAVEY 화면별 백엔드 구현 및 검증 보고서

작성일: 2026-09-07  
대상: `Server` / Java 21 / Spring Boot 3.5.1  
첨부 이미지: UI 16장 + 프로젝트 구조 2장, 총 18장

## 1. 적용 결과와 범위

실제 Server 소스에 누락 API를 구현했다. 문서만 작성한 작업이 아니다.
기존 `com.Wavey.WaveyService.domain.{도메인}` 및 `controller / dto / entity / repository / service` 구조와 `global` 공통 응답·예외·JWT 구조를 유지했다.

- 재사용: 장소·지역·콘텐츠 CRUD, 장소 지도 범위 조회, 루트 CRUD, 루트 스팟 추가/삭제/재정렬, 기존 인증·로그아웃.
- 보완: 기존 장소 목록 검색·정렬·필터, 장소 상세 데이터, 지역 영문명, 루트 목록·상세 경로 데이터, 루트 JWT principal 타입과 장소/순서 검증.
- 추가: 리뷰, 저장한 스팟, 프로필·설정, 장소별 콘텐츠 연결 조회, 주변 스팟, 스탬프·배지, 알림, 경로 계획·진행 상태, 약관·앱 메타데이터.
- 제외: 로고, 아이콘, 그리드/리스트 전환, 바텀시트, 애니메이션, 지도 줌·카메라·현재 위치 표시, 이미지 합성, OS 공유창, 화면 이동. 모두 FE 처리 항목이다.
- 실행되는 자체 REST API는 정상 동작한다. 주석 처리 대상은 서버의 **외부 데이터 API HTTP 호출**이다.
- TourAPI/촬영지 API와 Spotify oEmbed 호출 부분을 주석 처리했다. 새 경로 기능과 푸시 발송도 외부 호출을 실행하지 않는다.
- 기존 OAuth 로그인·토큰·권한 기능은 요구사항 외 기존 기능이므로 변경하지 않았다. 새 인증 우회나 개발용 로그인 API는 만들지 않았다.

작업 시작 전부터 장소 원천 데이터 필드·CSV import 관련 미커밋 수정이 있었다. 이를 되돌리지 않았다.
기존 `SpotConverter`, `ExternalSpotPayload`, `SpotExternalSyncServiceImpl`, CSV importer/bootstrap, `application.yml`에 이미 있던 변경은 이번 UI 구현으로 새로 만든 변경과 구분해야 한다.

## 2. 공통 API 계약

기본 경로: `/api/v1`  
인증: 기존 `Authorization: Bearer <accessToken>` 사용. 아래 UI API는 로그인 사용자를 기준으로 동작한다.

일반 응답:

```json
{
  "statusCode": 200,
  "message": "장소 목록 조회 성공",
  "data": []
}
```

리뷰 생성은 HTTP 201. 수정·조회·멱등 저장·스탬프 획득은 HTTP 200.
오류는 기존 형식 `{statusCode, error: {code, message}}`을 사용한다.

| 상태 | 의미 |
|---|---|
| 400 | 입력값·언어·카테고리 오류, 좌표 누락/범위 오류, 스탬프 거리 초과, 위치 설정 꺼짐, 루트 순서 중복 |
| 401 | 인증 없음 또는 유효하지 않은 JWT |
| 403 | 다른 사용자의 비공개 루트 접근·수정 |
| 404 | 장소/루트/스탬프 정의/약관 등 DB 데이터 없음, 타인 알림 ID |
| 409 | 중복 루트 스팟, 변경된 루트의 탐색 상태, 오래된 진행 버전 |
| 503 | 비활성화된 외부 데이터 호출 경로(`EXTERNAL_API_DISABLED`) |

외부 sync API는 기존 키·입력값 검증이 먼저 적용될 수 있어 설정에 따라 400도 반환한다. 어떠한 경우에도 해당 HTTP 요청은 실행하지 않는다.

### 언어와 카테고리

`language=ko|en` 선택값이 저장된 사용자 언어보다 우선한다. 생략 시 사용자 설정, 설정이 없으면 `ko`를 사용한다.
장소·콘텐츠·스탬프·배지·알림의 영문 DB 필드가 없으면 기존 한국어 원문을 반환한다. 자동 번역 API는 호출하지 않는다.
리뷰 원문과 사용자가 작성한 루트명은 자동 번역하지 않는다. 약관은 요청 언어의 시행 중인 DB 문서를 조회하며 없으면 404다.

| 기존 DB enum / 호환 입력 | 권장 카테고리 입력·응답 categoryCode | 한국어 categoryLabel | 영어 categoryLabel |
|---|---|---|---|
| K_DRAMA | K-DRAMA | 드라마 | K-DRAMA |
| K_POP | K-POP | 아이돌 | K-POP |
| K_MOVIE | K-MOVIE | 영화 | K-MOVIE |
| K_HERITAGE | K-HERITAGE | 관광지 | K-HERITAGE |

장소 목록은 기존 `category` 필드의 underscore enum을 유지하고 `categoryCode`, `categoryLabel`을 추가했다.
카테고리 전체 선택은 파라미터 생략이다. 지역 ID는 기존 `GET /regions` 결과를 사용하며 화면 예시의 지역 ID를 고정하지 않는다.

## 3. 전체 화면 대조표

번호는 첨부 순서를 기준으로 한다.

| 이미지 | 화면 | 구현 데이터·동작 | 사용 API |
|---|---|---|---|
| 1, 11 | 장소 상세 — 정보 | 장소·지역 주소·설명·별점·리뷰 수·대표 이미지·태그·영업시간·휴무·전화·교통편·좌표·저장 여부·포함 루트 ID·스탬프 획득 여부 | `GET /spots/{id}` |
| 2 | 홈 — 인기 스팟 | 리스트: 장소, 설명, 별점, 카테고리, 이미지, 좌표, 거리, 리뷰 수, 저장 여부. 검색·정렬·페이지·총 개수 | `GET /spots` |
| 3 | 내 루트 — 편집 | 장소 순서·이름·이미지·카테고리, 스팟 수, 총 거리·시간, 구간별 교통 안내, 이동수단 저장, 추가·삭제·재정렬·저장 | 기존 `/routes`, `/routes/{id}/spots` + `/plan`, `/travel-mode` |
| 4 | 경로 탐색 | 저장된 경로선·좌표, 현재/다음 스팟, 진행 인덱스, 완료 수, 이전/다음, 완료 여부 | `POST/GET/PATCH /routes/{id}/navigation` |
| 5 | 스탬프북 / 배지 컬렉션 | 전체 수집 수·방문 지역 수·배지 수·다음 배지까지 남은 수, 지역 필터, 획득/미획득 스탬프, 획득일·이미지, 배지별 진행률 | `GET /me/stamps`, `GET /me/badges` |
| 6 | 스탬프 획득 | 장소 스탬프 정의, 이름·설명·획득일·이미지 URL, 새 획득 여부, 획득 후 배지 상태 | `POST /spots/{id}/stamp`, `GET /me/stamps/{stampId}` |
| 7 | 마이페이지 | 이름·이메일·프로필 이미지·국가, 방문/루트/저장 스팟 수·배지 수, 프로필 편집 및 각 목록 | `GET/PATCH /me`, `/me/saved-spots`, 기존 `/routes`, `/me/stamps`, `/me/reviews` |
| 8 | 내 루트 목록 | 최신 루트명·스팟 수·거리·시간·미리보기 스팟과 좌표·이미지·경로선, 생성·이름 수정·삭제 | 기존 `GET/POST /routes`, `GET/PATCH/DELETE /routes/{id}` |
| 9 | 설정 | 푸시·위치·마케팅 설정, 언어 저장, 약관/개인정보 처리방침, 버전 | `GET/PATCH /me/settings`, `GET /app/documents/{type}`, `GET /app/metadata` |
| 10 | 알림 설정 | 메인 푸시, 스탬프, 루트 추천, 공지·업데이트 세부 설정 | 동일한 `GET/PATCH /me/settings` 재사용 |
| 12 | 장소 상세 — 콘텐츠 | 연관 드라마/영화·회차·촬영 설명·장면 URL, 음악·아티스트·길이·재생 URL·이미지, 관련 영상, 플레이리스트 URL | `GET /spots/{id}/contents` |
| 13 | 장소 상세 — 리뷰 | 평균 별점·리뷰 수, 작성자·국가·사진·작성일·본문·별점, 리뷰 작성 | `GET /spots/{id}`, `GET/POST /spots/{id}/reviews` |
| 14 | 장소 상세 — 주변 스팟 | 현재 장소 제외, 가까운 장소·주소·설명·별점·이미지·거리 | `GET /spots/{id}/nearby` |
| 15, 16 | 프로젝트 폴더 구조 | 기존 Server 패키지/계층 유지, 필요한 review/stamp/notification 도메인 추가 | UI 화면 아님 |
| 17 | 홈 — 상세 필터 | 지역·카테고리·최소 평점·반경을 검색/정렬과 동시 적용 | 기존 `GET /regions` + 확장 `GET /spots` |
| 18 | 알림 목록 | 사용자별 알림·미읽음 수·타입·시간·대상 ID, 개별/모두 읽음 | `GET /me/notifications`, `PATCH /me/notifications/{id}/read`, `PATCH /me/notifications/read-all` |

홈의 알림 숫자는 `GET /me/notifications?size=1`의 `unreadCount`로 표시한다.
공유 버튼에는 이미 조회한 장소·스탬프·스탬프북 데이터를 사용하며, 별도 전송/공유 API를 중복 구현하지 않았다.
자동 스탬프 인식은 FE가 위치 권한과 근접을 감지한 후 동일한 획득 API를 호출하는 방식이다.

## 4. 홈 검색·정렬·필터

`GET /api/v1/spots`

| 파라미터 | 기본값 / 범위 | 동작 |
|---|---|---|
| keyword | 선택, 최대 200자 | 한국어/영어 장소명, 기존 작품명, 연결된 콘텐츠 제목 검색 |
| category | 선택 | 위 4개 카테고리. underscore enum도 호환 |
| regionId | 선택 | DB 지역 ID |
| minRating | 선택, 0–5 | 3.0 / 4.0 / 4.5 이상 필터 |
| sort | POPULAR | POPULAR / RATING / LATEST / DISTANCE |
| latitude, longitude | 선택 | 각각 -90–90 / -180–180, 함께 전달 |
| radiusMeters | 선택, 1–100000 | 1km=1000, 3km=3000, 5km=5000 |
| page, size | 0, 20 | page 0–10000 / size 1–100 |
| language | 사용자 설정 | ko / en |

거리순 또는 반경을 지정하면 좌표가 필수다. NaN·Infinity·범위 밖 좌표는 허용하지 않는다.
Haversine 거리 계산·필터·정렬·페이지 처리는 DB 쿼리에서 수행한다. 직선거리이며 경로 이동 거리와 다르다.

인기순은 `savedCount + reviewCount` 내림차순으로 정의했다. 평점순은 평균 별점, 최신순은 생성일, 거리순은 직선거리 오름차순이다. 동률은 장소 ID 내림차순으로 고정한다.
저장 해제 시 저장 카운트가 감소하며, 동일 사용자의 반복 저장/해제는 중복 계산하지 않는다.

```http
GET /api/v1/spots?category=K-DRAMA&regionId=1&minRating=4.5&sort=DISTANCE&latitude=37.5796&longitude=126.977&radiusMeters=3000&language=ko&page=0&size=20
Authorization: Bearer <accessToken>
```

기존 목록 응답의 `data: []` 형식을 유지한다. 총 필터 결과 수는 `X-Total-Count`, 총 페이지는 `X-Total-Pages` 응답 헤더다. CORS 노출 헤더도 추가했다.
기존 무제한 목록 조회는 기본 20개 페이지 조회로 바뀌므로 FE는 다음 페이지를 요청해야 한다.

카드 필드:

```text
spotId, regionId, name, description, category, categoryCode, categoryLabel,
address, latitude, longitude, thumbnailUrl, avgRating, reviewCount, saved, distanceMeters
```

주변 스팟은 `GET /spots/{id}/nearby?radiusMeters=5000&size=10&language=ko`.
기본 반경 5km, 크기 최대 99, 현재 장소 제외 후 거리순이다.

## 5. 장소 상세·콘텐츠·리뷰

### 정보

기존 상세 응답 필드를 유지하고 다음을 추가했다:

```text
categoryCode, categoryLabel, transportInfo, playlistUrl,
mapProvider=GOOGLE_MAPS, googleMapsUrl,
reviewCount, saved, stampAcquired, routeIds, tags
```

운영시간·휴무·주소·교통편·전화·이미지는 모두 DB 값이다.
경복궁·해운대 등의 특정 ID나 화면의 예시 별점/리뷰 수를 하드코딩하지 않는다.

### 콘텐츠

`GET /spots/{id}/contents` 응답:

```text
dramas[], movies[], music[], videos[], playlistUrl
```

각 콘텐츠:

```text
contentId, kind, title, artist, episodes, description, sceneDescription,
thumbnailUrl, playbackUrl, sceneUrl, durationSeconds
```

기존 `contents`에 저장된 콘텐츠를 신규 `spot_contents` 연결 테이블로 참조한다.
외부 스트리밍 검색·재생·다운로드는 하지 않는다. 실제 플레이어 및 URL 열기는 FE 담당이다.
재생 진행 바는 플레이어 상태이며 서버 API를 만들지 않았다.

### 리뷰 작성

`POST /spots/{spotId}/reviews`

```json
{
  "rating": 5,
  "body": "아침에 방문하니 조용해서 좋았어요.",
  "countryCode": "KR",
  "language": "ko"
}
```

별점은 필수 정수 1–5, 본문은 공백 제외 필수·최대 2000자. 국가 코드는 선택 대문자 2자리이며 언어는 선택값이다.
작성자 ID는 JWT에서 가져오며 요청 본문에서 받지 않는다. 작성 시간은 서버에서 기록한다.
장소 행 잠금과 트랜잭션으로 리뷰 저장 후 평균·리뷰 수를 함께 갱신한다.
한 사용자의 여러 리뷰는 허용한다. 사용자당 1회 제한은 요구되지 않았으므로 임의로 추가하지 않았다.

- `GET /spots/{id}/reviews?page=0&size=20`: 해당 장소 리뷰, 최신순.
- `GET /me/reviews?page=0&size=20`: 본인이 작성한 리뷰만.
- 두 목록의 data는 `content`, `totalElements`, `totalPages` 등을 가진 페이지 응답이다.
- 작성자 이름·사진·국가·작성 시각·본문·별점, 장소명·이미지를 제공한다.
- 국가 정보는 리뷰 작성 요청의 `countryCode`를 사용한다.

## 6. 루트·탐색

기존 API를 재사용한다:

```text
GET    /routes
POST   /routes
GET    /routes/{routeId}
PATCH  /routes/{routeId}
DELETE /routes/{routeId}
POST   /routes/{routeId}/spots
PATCH  /routes/{routeId}/spots/reorder
DELETE /routes/{routeId}/spots/{routeSpotId}
```

기존 루트 컨트롤러가 `UserDetails`를 받던 부분을 실제 JWT principal인 `User`로 맞췄다.
자신의 루트만 편집할 수 있고, 비공개 루트는 소유자만 조회 가능하다.
없는 장소·중복 장소·중복 순서 입력을 검증한다. 재정렬은 전체 routeSpot ID 목록을 전달한다.
루트 목록은 최신 생성순이며 `visibility` 기존 필터를 그대로 사용한다. FE 정렬 전환은 반환된 목록으로 처리할 수 있다.

목록·상세 응답에 `plan`을 추가했다. 기본 이동수단은 TRANSIT이다.

`GET /routes/{id}/plan?mode=TRANSIT&language=en`:

```text
mapProvider, mode, spotCount, distanceMeters, durationSeconds, routeAvailable,
spots[], legs[]
```

각 leg는 `fromSpotId, toSpotId, distanceMeters, durationSeconds, encodedPolyline, instruction, available`이다.
WALKING=도보 / TRANSIT=대중교통 / DRIVING=자동차.
총 거리/시간은 저장된 구간을 합산하며, 하나라도 없으면 총합은 null, `routeAvailable=false`를 반환한다.
다른 이동수단의 값이나 임의 속도로 시간을 만들어내지 않는다.
스팟 0~1개 루트는 이동 구간이 없으므로 합계 0이다.

`PATCH /routes/{id}/travel-mode`:

```json
{"mode":"WALKING"}
```

탐색 API:

```text
POST  /routes/{id}/navigation?mode=TRANSIT
GET   /routes/{id}/navigation
PATCH /routes/{id}/navigation
```

진행 변경 요청:

```json
{"direction":"NEXT","expectedIndex":0,"expectedVersion":0}
```

`direction`은 NEXT / PREVIOUS. 응답에서 받은 최신 `currentIndex`, `version`을 다음 요청에 전달한다.
응답은 `routeId, plan, currentIndex, completedSpotCount, completed, currentSpotId, nextSpotId, version`.
인덱스는 0부터 시작하며 화면 번호는 FE에서 +1한다.
현재 장소보다 앞의 장소를 완료한 것으로 계산하고, 마지막 장소에서 NEXT를 누르면 전체 완료다.
시작 시 완료 수는 0이다. 이전 버튼과 완료 취소도 처리한다.

탐색 시작 시 스팟 ID 순서를 DB에 저장한다. 이후 루트 순서나 구성이 바뀌면 409를 반환하므로 탐색을 다시 시작해야 한다.
오래된 버전/인덱스의 진행 요청도 409로 거절한다.
경로 데이터가 없어도 스팟 목록 기반 탐색 진행은 가능하며, 지도 경로 안내는 `routeAvailable`을 확인해야 한다.
탐색 진행은 스탬프 획득과 별개이며 방문 인증으로 자동 인정하지 않는다.

### Google Maps 적용 범위

백엔드는 Google Maps를 사용할 수 있도록 DB 좌표·이동수단·저장 encoded polyline 및 `mapProvider`를 반환한다.
지도 SDK 자체는 FE에서 Google Maps로 구성한다. 화면 예시의 Leaflet/OpenStreetMap용 코드나 의존성을 새로 추가하지 않았다.
장소의 `googleMapsUrl`은 `api=1&query=위도%2C경도` 형식이다. [Google Maps URL 공식 문서](https://developers.google.com/maps/documentation/urls/get-started)

향후 외부 경로 수집이 승인되면 별도 수집 과정에서 [Google Routes API](https://developers.google.com/maps/documentation/routes/compute_route_directions)의 경로 데이터를 DB에 저장할 수 있다.
현재 구현은 Google API를 호출하지 않으며, 실제 지도 SDK 화면·네트워크 동작은 이번 백엔드 테스트 대상이 아니다.

## 7. 스탬프·배지

`POST /spots/{spotId}/stamp`:

```json
{"latitude":35.1587,"longitude":129.1604}
```

- DB에 장소 및 해당 장소의 스탬프 정의가 있어야 한다.
- 사용자 위치 설정과 좌표를 검증하고, 장소별 `radiusMeters` 이내에서만 신규 획득한다. 기본 반경은 150m로 정했다.
- 획득 시각·지역·사용자·장소를 서버에서 기록한다. 사용자 좌표 자체를 DB에 저장하지 않는다.
- 같은 스탬프 재요청은 최초 기록을 반환하며 `newlyAcquired=false`다. 사용자 행 잠금과 유일 제약을 사용한다.
- 새 획득일 때만 배지 조건을 평가하고 스탬프 알림을 DB에 기록한다.
- `stampEnabled=false`이면 해당 자동 인앱 스탬프 알림을 만들지 않는다.
- GPS 좌표는 클라이언트 제공값이므로 OS 위치 위변조 감지·실기기 검증까지 제공하는 기능은 아니다.

`GET /me/stamps?regionId={id}`는 전체 수집 요약과 해당 지역의 획득/미획득 스탬프 목록을 반환한다.
지역 필터는 목록에만 적용하며 요약은 사용자 전체 여행 기준이다.

```text
collectedCount, visitedRegionCount, badgeCount, remainingToNextBadge,
stamps[] = {stampId, spotId, regionId, name, description, imageUrl, acquired, acquiredAt}
```

**해운대해변 스탬프 획득 화면에도 `imageUrl` 필드가 존재한다.**
이미지가 아직 없으면 `"imageUrl": null`이며 필드 자체는 유지된다.
추후 `stamps.image_url`에 URL을 저장하면 획득 응답·상세·스탬프북에 반영된다.
다른 임시 이미지나 배경색을 서버에서 생성하지 않았다.

`GET /me/badges`:

```text
badgeId, name, description, imageUrl, requiredStamps, progress, remaining, acquired, acquiredAt
```

배지 정의의 필수 스탬프 수, 선택 지역·카테고리 조건을 모두 적용한다.
획득 기록은 영속 저장한다. `remainingToNextBadge`는 미획득 배지 중 최소 남은 수이며, 정의가 없거나 모두 획득했으면 null이다.
화면 예시의 배지명·조건은 확정된 규칙으로 제공되지 않았으므로 DB 정의로 관리한다.

## 8. 마이페이지·설정·알림·문서

### 프로필 및 저장한 스팟

`GET /me`:

```text
userId, name, email, profileImageUrl, countryCode,
visitedSpotCount, routeSpotCount, savedSpotCount, badgeCount
```

방문 수=획득 스탬프 장소 수, 루트 스팟 수=본인 전체 루트의 중복 제외 장소 수, 저장 수=현재 저장한 장소 수.
새 카운트를 화면 숫자로 고정하지 않았다.

`PATCH /me`:

```json
{"name":"여행자_시영","profileImageUrl":"https://example.com/profile.png","countryCode":"KR"}
```

이름 필수, 이미지는 HTTPS URL·최대 1000자. 이메일·권한은 변경하지 않는다.
이미지 파일 업로드/스토리지 기능은 요청 화면에서 필수로 정해지지 않았으므로 추가하지 않았으며, 준비된 이미지 URL을 저장한다.

```text
GET    /me/saved-spots?page=0&size=20&language=ko
PUT    /me/saved-spots/{spotId}
DELETE /me/saved-spots/{spotId}
```

저장 목록은 최신 저장순. 다른 사용자의 저장 목록은 노출하지 않는다.

### 설정

`GET/PATCH /me/settings`를 일반 설정과 알림 설정 화면이 함께 사용한다.

```json
{
  "language":"en",
  "pushEnabled":true,
  "stampEnabled":true,
  "routeEnabled":true,
  "noticeEnabled":true,
  "locationEnabled":true,
  "marketingEnabled":false
}
```

PATCH는 전달한 항목만 수정한다. 초기값은 위와 같으며 language만 ko가 기본이다.
OS 위치·푸시 권한은 FE에서 별도 요청해야 한다. 이 API는 서버에 저장된 사용자 선호값이다.
`pushEnabled`는 외부 푸시 전송 선호이며 인앱 목록을 숨기는 값은 아니다.
루트 추천·공지·마케팅 발송 연동은 현재 실행하지 않는다. 저장된 설정과 알림 데이터를 제공하며, 외부 발송 작업을 생성하지 않았다.

### 알림

- `GET /me/notifications?page=0&size=20&language=ko`: `unreadCount` + `notifications` 페이지.
- 알림 항목: `id, type, title, body, targetType, targetId, createdAt, readAt`.
- `PATCH /me/notifications/{id}/read`: 본인 알림 개별 읽음.
- `PATCH /me/notifications/read-all`: 아직 안 읽은 본인 알림만 갱신하며 변경 건수 반환.
- 반복 읽음은 멱등. 다른 사용자의 알림 ID는 404.
- “3시간 전/어제”, 읽음 표시, 알림 대상 화면 이동은 FE에서 시간·상태·target 필드를 이용한다.
- 스탬프 알림은 신규 획득에서 생성한다. 추천 루트/새 리뷰/배지 진행/공지 예시는 `notifications`에 저장된 데이터를 조회한다. 현재 화면 밖의 추천 알고리즘이나 외부 이벤트 수집 시스템은 추가하지 않았다.

### 약관·버전·로그아웃

- `GET /app/documents/PRIVACY?language=ko`
- `GET /app/documents/TERMS?language=en`
- `GET /app/metadata`: 앱 버전, GOOGLE_MAPS, 지원 언어, 카테고리·배지명, 정렬값.
- 문서는 `effectiveAt <= 현재 시각`인 최신 시행 문서만 제공한다.
- 버전 기본값은 1.0.0이며 `wavey.app-version`으로 조정 가능하다.
- 로그아웃은 기존 `POST /auth/logout/{id}`를 그대로 사용한다. 기존 인증 코드 동작은 변경하지 않았다.

## 9. DB 준비와 외부 호출 비활성화

### 신규 저장 구조

| 테이블 | 용도 |
|---|---|
| reviews | 리뷰 본문·별점·작성자·언어·국가 |
| saved_spots | 사용자별 저장 장소, 사용자+장소 유일 |
| user_settings | 언어·설정·프로필 이미지·국가, 사용자 유일 |
| spot_contents | 기존 콘텐츠와 장소 연결, 분류·회차·장면·음악 정보 |
| stamps | 장소별 스탬프 정의, image_url 포함 |
| user_stamps | 획득 이력, 사용자+스탬프 유일 |
| badges | 배지 조건·이미지·한국어/영어 |
| user_badges | 배지 획득 이력, 사용자+배지 유일 |
| notifications | 사용자별 인앱 알림·읽음 상태 |
| route_legs | 출발/도착/이동수단별 저장 거리·시간·폴리라인·안내 |
| route_navigation | 루트별 탐색 순서·현재 인덱스·완료·낙관적 잠금 버전 |
| app_documents | 문서 종류·언어·버전·시행일·본문 |

기존 spots에는 영문명·영문주소·영문설명·영문운영시간/휴무·교통 안내·플레이리스트·리뷰/저장 수를 추가했다.
기존 regions에는 `name_en`, routes에는 `travel_mode`를 추가했다.

### 기존 DB 적용

현재 프로젝트의 `ddl-auto=update` 운영 방식을 변경하지 않았다.
기존 행이 있는 DB는 서버 갱신 전 [추가 컬럼 SQL](sql/ui-additive-columns.sql)을 먼저 실행해 새 NOT NULL 카운터와 이동수단 기본값을 준비한다.
그 후 기존 JPA update 설정으로 새 테이블·나머지 nullable 컬럼을 추가할 수 있다.
이 SQL은 자동 실행 migration이 아니며 운영 DB에 실행하지 않았다. 별도 migration 체계를 쓰는 환경이라면 이 변경을 해당 체계에 반영해야 한다.

### 화면을 채우기 위해 DB에 필요한 데이터

1. 기존 regions/spots: 지역·좌표·설명·이미지·평점·운영시간 등 실제 데이터.
2. 영문 필드: 장소, 지역, 콘텐츠 연결, 스탬프, 배지, 알림의 준비된 번역.
3. 기존 contents + spot_contents: 작품/음악/영상 연결, 회차·아티스트·장면 설명·재생 URL·이미지.
4. stamps: 각 장소의 정의와 반경. 해운대해변도 해당 spot_id에 행이 있어야 한다. image_url은 추후 입력 가능.
5. badges: 확정된 이름·조건·이미지. required_stamps는 양수로 관리한다.
6. route_legs: 각 방향과 각 이동수단별 거리(m)·시간(s)·필요하면 encoded_polyline. 반대 방향 데이터를 자동 재사용하지 않는다.
7. app_documents: 승인된 개인정보 처리방침·이용약관 본문. 실제 법률 문구를 임의로 작성하지 않았다.
8. notifications: 공지·추천 등 DB 전용 표시 데이터.

이 작업은 기존 실제 DB 행을 덮어쓰거나 스크린샷의 숫자를 seed로 주입하지 않았다.
새 테이블이 비어 있으면 목록은 빈 배열, 정의가 필요한 단건 API는 404다.
DB에 없는 이미지·경로 시간·번역·법률 본문을 외부에서 자동 생성하지 않는다.

외부 호출 변경 위치:

- `domain/spot/external/client/TourApiSpotClient.java`: 관광지 HTTP·썸네일 HTTP 주석 처리. 썸네일 보강은 비활성 시 저장 데이터를 유지한다.
- `domain/spot/external/client/MediaLocationSpotClient.java`: 촬영지 HTTP 주석 처리.
- `domain/content/service/ContentService.java`: Spotify oEmbed 주석 처리. 기존 곡의 저장 썸네일 유지, 신규 곡에 저장 이미지가 없으면 빈 문자열.
- `domain/route/service/RoutePlanningService.java`: Google 호출 없이 route_legs 조회.
- `domain/notification/service/NotificationService.java`: DB 알림만 저장, push 호출 주석.

기존 스케줄러/CSV bootstrap 기본 비활성 설정을 변경하지 않았다. 개발 중 환경 변수로 자동 import를 켜 놓았다면 DB 전용 실행 시 해당 설정도 꺼서 사용한다.

## 10. 테스트 결과

최종 실행 명령:

```powershell
cd Server
.\gradlew.bat test --offline --console=plain
```

테스트 환경: Java 21, Spring Boot, MockMvc, H2 인메모리 DB.
통합 테스트는 각 테스트의 데이터를 트랜잭션으로 롤백한다. 실제 DB·사용자 계정·외부 제공자에 쓰지 않는다.

| 테스트 묶음 | 수 | 결과 |
|---|---:|---|
| UiApiIntegrationTest | 18 | 통과 |
| 기존 RouteSpotServiceTest | 4 | 통과 |
| 기존 WaveyServiceApplicationTests | 1 | 통과 |
| 총계 | 23 | 실패 0, 오류 0, 건너뜀 0 |

주요 검증:

- 인증 없는 접근, 실제 발급 JWT를 통한 기존 루트 CRUD/추가/삭제.
- 카테고리·언어·위경도·페이지·리뷰 입력 검증.
- 작품명·영문 연결 콘텐츠 검색, 복합 필터, 거리순/반경, 최신순/인기순/평점순.
- 장소 상세·콘텐츠 그룹·촬영 설명·주변 장소 제외 및 반환·지역 영문명.
- 리뷰 저장·평균 평점/리뷰 수 갱신·본인 리뷰 분리.
- 저장/해제 멱등성·인기 카운트·사용자별 목록 분리.
- 프로필 변경·부분 설정 저장·저장 언어 적용.
- 스탬프 imageUrl 필드 null 유지 및 이미지 URL 반영.
- 거리 초과·위치 설정 꺼짐·중복 스탬프·중복 배지/알림 방지.
- 지역별 배지 조건·마이페이지 방문 수·스탬프 알림 설정.
- 알림 읽음 소유권·모두 읽음 멱등성·영문 알림.
- 저장 경로 거리/시간·이동수단별 경로 없음·비공개 루트 차단.
- 이전/다음/완료·오래된 탐색 요청·루트 재정렬 후 진행 충돌.
- 없는 장소의 루트 생성·중복 재정렬 거절.
- 시행 중인 약관 선택·미래 버전 제외.
- 외부 장소 데이터 호출 비활성 및 Spotify DB 이미지 보존.

[테스트 HTML 보고서](../build/reports/tests/test/index.html)  
[통합 테스트 소스](../src/test/java/com/Wavey/WaveyService/UiApiIntegrationTest.java)

검증 범위는 화면 기능에 대응하는 백엔드 API와 H2 DB 통합 동작이다.
실제 FE 화면 렌더링, Google Maps SDK 표시, 실기기 GPS·푸시, 운영 PostgreSQL 배포 및 부하/동시성 스트레스 테스트는 수행하지 않았다.
행 잠금·유일 제약·버전 검증은 구현했으며, 테스트에서는 중복/오래된 순차 요청을 검증했다.
