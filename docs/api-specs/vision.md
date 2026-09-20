# Vision(Docent) API 명세서

카메라로 촬영한 이미지를 분석해 번역, 문화유산 정보, 웹 검색 결과를 제공하는 "AI 도슨트" 기능입니다.

## 1. 설계 메모

- Base path: `/api/v1/vision`
- `multipart/form-data` 요청.
- 로그인 유저는 유저 기준, 비로그인(익명)은 IP 기준으로 요청 횟수 제한(rate limit) 적용.
- 이미지: 최대 5MB, `image/jpeg` / `image/png` / `image/webp`만 허용. 서버가 실제 바이트를 디코딩해서 선언된 Content-Type과 일치하는지도 검증함(위조 방지).

---

## 2. 이미지 분석

```
POST /api/v1/vision/analyze
Content-Type: multipart/form-data
Authorization: Bearer {accessToken}   // 선택 — 없으면 IP 기준 rate limit
```

### Request (multipart parts)

| 파트명 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `file` | 파일 | O | 이미지 파일 (jpeg/png/webp, ≤5MB) |
| `features` | `Set<VisionFeature>` (쿼리/폼 파라미터, 여러 개 가능) | O (1개 이상) | `TRANSLATION`, `HERITAGE`, `WEB_SEARCH` |
| `lat` | Double | 선택 | 위도 (HERITAGE 기능에서만 사용) |
| `lng` | Double | 선택 | 경도 (HERITAGE 기능에서만 사용) |

### Response `VisionAnalysisResponse`

```ts
{
  translation: TranslationResponse | null   // TRANSLATION 요청 시
  heritage: HeritageResponse[] | null       // HERITAGE 요청 시
  webSearch: WebDetectionResponse | null    // WEB_SEARCH 요청 시
}
```

**`TranslationResponse`**
```ts
{
  originalText: string
  translatedText: string
  terms: CulturalTermResponse[]
}
```

**`CulturalTermResponse`**
```ts
{
  original: string
  translatedName: string
  koreanDescription: string
  description: string
  category: string
  domain: string
  source: string
  translationSource: string
  descriptionSource: string
  ambiguous: boolean
}
```

**`HeritageResponse`**
```ts
{
  id: string
  koreanName: string
  englishName: string
  englishNameSource: string
  address: string
  detailedAddress: string
  designationType: string
  koreanDescription: string
  englishDescription: string
  descriptionSource: string
  translationRequired: boolean
}
```

**`WebDetectionResponse`**
```ts
{
  bestGuessLabels: string[]
  webEntities: string[]
  pagesWithImages: { title: string, url: string }[]
}
```

---

## 3. 검증 규칙

- 파일 비어있음 → `COMMON_FILE_EMPTY`
- 파일 5MB 초과 → `COMMON_FILE_SIZE_EXCEEDED`
- 지원하지 않는/위조된 이미지 형식 → `VISION_IMAGE_TYPE_UNSUPPORTED`
- `features` 비어있음/누락 → `COMMON_INVALID_PARAMETER`
- 요청 횟수 초과 → `VISION_RATE_LIMIT_EXCEEDED` (429)

---

## 4. 에러 코드

| 코드 | HTTP | 설명 |
|---|---|---|
| `COMMON_FILE` (`COMMON_FILE_EMPTY`) | 400 | 파일이 비어있거나 유효하지 않음 |
| `COMMON_FILE_SIZE` (`COMMON_FILE_SIZE_EXCEEDED`) | 400 | 파일 용량 초과 |
| `VISION_400_IMAGE_TYPE` | 400 | 지원하지 않는 이미지 형식 (JPEG/PNG/WEBP만 허용) |
| `COMMON_INVALID_PARAMETER` | 400 | 요청 파라미터 오류 (예: features 누락) |
| `VISION_429_RATE_LIMIT` | 429 | 이미지 분석 요청 과다, 잠시 후 재시도 |
| `VISION_500_FAILED` | 500 | 이미지 분석 실패 |
| `VISION_429_QUOTA` | 429 | 분석 토큰/쿼터 소진 |
| `TRANSLATION_502_FAILED` (`GOOGLE_TRANSLATION_FAILED`) | 502 | 번역 API 실패 |

프론트 참고: `@ApiResponses`에 명시된 응답 코드는 `200, 400, 401, 429, 500`.

---

## 5. 미구현 / 후속 과제

- Rate limit 임계값(횟수/기간) 및 초과 시 재시도 가능 시점 안내를 프론트에 노출할지 여부 확인 필요
