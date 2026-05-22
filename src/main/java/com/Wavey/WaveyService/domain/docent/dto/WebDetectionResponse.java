package com.Wavey.WaveyService.domain.docent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class WebDetectionResponse {
    private List<String> bestGuessLabels;        // 베스트 게스 (최적 예상 라벨)
    private List<String> webEntities;           // 웹 엔티티 (관련 키워드)
    private List<WebPageInfo> pagesWithImages;  // 이미지가 포함된 웹 페이지 목록

    @Getter
    @AllArgsConstructor
    public static class WebPageInfo {
        private String title; // 웹 페이지 제목
        private String url;   // 웹 페이지 주소
    }
}