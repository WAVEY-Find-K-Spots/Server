package com.Wavey.WaveyService.domain.docent.client;

import com.Wavey.WaveyService.domain.docent.dto.WebDetectionRawData;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.type.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
/**
 * 이미지 파일을 Google Vision의 raw 분석 결과로 변환하는 외부 연동 계층입니다.
 *
 * <p>도메인 후처리는 담당하지 않으며 실제 Vision 호출이 필요한 지점을 이 클래스에
 * 한정합니다. 후처리 서비스는 이 클래스의 결과를 raw 데이터로 전달받습니다.</p>
 */
@Service
@RequiredArgsConstructor
public class GoogleVisionClient {

    private final CloudVisionTemplate cloudVisionTemplate;
    private final ImageAnnotatorClient imageAnnotatorClient; // 복잡한 옵션 적용을 위해 추가 주입

    /**
     * 1. 번역용 텍스트 추출 (TEXT_DETECTION)
     */
    public String extractText(Resource imageResource) {
        try {
            AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                    imageResource, Feature.Type.TEXT_DETECTION);

            if (response.hasError()) {
                throw new CustomException(ErrorCode.VISION_ANALYSIS_FAILED);
            }

            String text = response.getFullTextAnnotation().getText();

            if (text.isEmpty()) {
                log.info("Vision AI: 추출된 텍스트가 없습니다.");
                return "";
            }
            return text;
        } catch (Exception e) {
            log.error("Vision AI OCR Error", e);
            throw mapVisionException(e);
        }
    }

    /**
     * 2. 문화재 정보 추출 (LANDMARK_DETECTION - 좌표 범위 기반 정확도 향상)
     */
    public List<String> detectLandmarks(Resource imageResource, Double lat, Double lng) {
        try (InputStream inputStream = imageResource.getInputStream()) {
            // 1. 이미지 변환
            ByteString imgBytes = ByteString.readFrom(inputStream);
            Image image = Image.newBuilder().setContent(imgBytes).build();

            // 2. 랜드마크 감지 기능 설정
            Feature feature = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();

            // 3. 요청 빌더 생성
            AnnotateImageRequest.Builder requestBuilder = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image);

            // 4. 좌표 값이 존재하면 Bounding Box(검색 범위) 설정
            if (lat != null && lng != null) {
                // ±0.01도는 위도/경도 상 대략 1km 반경을 의미합니다.
                double offset = 0.01;

                LatLng southwest = LatLng.newBuilder()
                        .setLatitude(lat - offset)
                        .setLongitude(lng - offset)
                        .build();

                LatLng northeast = LatLng.newBuilder()
                        .setLatitude(lat + offset)
                        .setLongitude(lng + offset)
                        .build();

                LatLongRect boundingBox = LatLongRect.newBuilder()
                        .setMinLatLng(southwest)
                        .setMaxLatLng(northeast)
                        .build();

                ImageContext imageContext = ImageContext.newBuilder()
                        .setLatLongRect(boundingBox)
                        .build();

                requestBuilder.setImageContext(imageContext);
                log.info("Vision AI: 좌표 기반 랜드마크 탐색 적용 (lat: {}, lng: {})", lat, lng);
            }

            // 5. API 호출 (ImageAnnotatorClient 직접 사용)
            BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(List.of(requestBuilder.build()));
            AnnotateImageResponse res = response.getResponsesList().getFirst();

            if (res.hasError()) {
                log.error("Vision API Error: {}", res.getError().getMessage());
                throw new CustomException(ErrorCode.VISION_ANALYSIS_FAILED);
            }

            return res.getLandmarkAnnotationsList().stream()
                    .map(EntityAnnotation::getDescription)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Vision AI Landmark Error: {}", e.getMessage());
            throw mapVisionException(e);
        }
    }

    //3. 물건/장소 웹 검색 정보 (WEB_DETECTION - 베스트게스, 엔티티, 웹페이지 종합 반환)
    public WebDetectionRawData detectWeb(Resource imageResource) {
        try {
            AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                    imageResource, Feature.Type.WEB_DETECTION);

            if (response.hasError()) {
                throw new CustomException(ErrorCode.VISION_ANALYSIS_FAILED);
            }

            WebDetection webDetection = response.getWebDetection();

            // 1. 최적 예상 라벨 (Best Guess) 추출
            List<String> bestGuessLabels = webDetection.getBestGuessLabelsList().stream()
                    .map(WebDetection.WebLabel::getLabel)
                    .collect(Collectors.toList());

            // 2. 웹 엔티티 (Entities) 추출 (최대 5개)
            List<String> webEntities = webDetection.getWebEntitiesList().stream()
                    .map(WebDetection.WebEntity::getDescription)
                    .collect(Collectors.toList());

            // 3. 후처리 전 웹페이지 raw 데이터 추출
            List<WebDetectionRawData.WebPage> pages = webDetection.getPagesWithMatchingImagesList().stream()
                    .map(page -> new WebDetectionRawData.WebPage(page.getPageTitle(), page.getUrl()))
                    .collect(Collectors.toList());

            return new WebDetectionRawData(bestGuessLabels, webEntities, pages);

        } catch (Exception e) {
            log.error("Vision AI Web Detection Error: {}", e.getMessage());
            throw mapVisionException(e);
        }
    }

    private CustomException mapVisionException(Exception e) {
        if (e instanceof com.google.api.gax.rpc.ApiException apiException) {

            if (apiException.getStatusCode().getCode() == com.google.api.gax.rpc.StatusCode.Code.RESOURCE_EXHAUSTED) {
                return new CustomException(ErrorCode.VISION_TOKEN_EXHAUSTED);
            }
        }
        return new CustomException(ErrorCode.VISION_ANALYSIS_FAILED);
    }
}
