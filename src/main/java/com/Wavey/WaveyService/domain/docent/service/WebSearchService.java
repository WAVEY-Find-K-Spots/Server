package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.dto.WebDetectionRawData;
import com.Wavey.WaveyService.domain.docent.dto.WebDetectionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Google Vision Web Detection 결과를 서비스 응답으로 가공하는 내부 서비스입니다.
 *
 * <p>이미지 파일이 아닌 구조화된 raw 결과를 받는 것은 의도된 설계입니다.
 * 실제 Google API 없이 필터링, 정렬 및 응답 변환을 단위 테스트할 수 있습니다.</p>
 */
@Service
public class WebSearchService {

    public WebDetectionResponse process(WebDetectionRawData rawData) {
        List<String> entities = rawData.getWebEntities().stream()
                .filter(description -> description != null && !description.isBlank())
                .limit(5)
                .toList();
        List<WebDetectionResponse.WebPageInfo> pages = rawData.getPagesWithImages().stream()
                .filter(page -> page.getTitle() != null && !page.getTitle().isBlank())
                .limit(3)
                .map(page -> new WebDetectionResponse.WebPageInfo(page.getTitle(), page.getUrl()))
                .toList();

        return new WebDetectionResponse(rawData.getBestGuessLabels(), entities, pages);
    }
}
