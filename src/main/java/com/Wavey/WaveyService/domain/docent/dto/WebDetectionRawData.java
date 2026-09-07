package com.Wavey.WaveyService.domain.docent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Google Vision 응답을 후처리 서비스에 전달하기 위한 내부 raw 데이터입니다.
 * 외부 API 응답 DTO와 분리하여 테스트 픽스처를 간단히 만들 수 있습니다.
 */
@Getter
@AllArgsConstructor
public class WebDetectionRawData {

    private List<String> bestGuessLabels;
    private List<String> webEntities;
    private List<WebPage> pagesWithImages;

    @Getter
    @AllArgsConstructor
    public static class WebPage {
        private String title;
        private String url;
    }
}
