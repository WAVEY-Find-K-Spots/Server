package com.Wavey.WaveyService.domain.content.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 자동 수집 컷오프. 코드 재배포 없이 허용/금지 단어와 개수 캡을 바꿀 수 있게 yml에 둔다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "media-collect")
public class MediaCollectProperties {

    private Youtube youtube = new Youtube();
    private Heritage heritage = new Heritage();
    private Spotify spotify = new Spotify();

    @Getter
    @Setter
    public static class Youtube {
        private int maxDurationSec = 1200;
        private int maxKeep = 8;
        /** 같은 content 재수집 시 YouTube search 생략. 0이면 캐시 끔. */
        private int refreshCacheHours = 24;
        private List<String> allowKeywords = new ArrayList<>(List.of(
                "예고편", "티저", "trailer", "teaser",
                "메이킹", "비하인드", "behind", "making",
                "OST", "MV", "뮤직비디오"
        ));
        private List<String> denyKeywords = new ArrayList<>(List.of(
                "다시보기", "본편", "풀버전", "풀영상",
                "몰아보기", "전회차", "무료보기", "VOD", "스트리밍"
        ));
    }

    @Getter
    @Setter
    public static class Heritage {
        private int minDurationSec = 60;
        private int maxDurationSec = 1_200;
        private List<String> allowKeywords = new ArrayList<>(List.of(
                "소개", "소개영상", "문화유산", "한국의 문화유산",
                "국가유산", "국보", "보물", "사적",
                "세계유산", "unesco", "heritage", "유적"
        ));
        private List<String> denyKeywords = new ArrayList<>(List.of(
                "먹방", "맛집", "숙소", "호텔", "브이로그", "vlog",
                "쇼츠", "shorts", "몰아보기", "full", "asmr"
        ));
        private List<String> trustedChannelKeywords = new ArrayList<>(List.of(
                "국가유산", "k-heritage", "문화재청", "unesco", "korea heritage"
        ));
    }

    @Getter
    @Setter
    public static class Spotify {
        private int maxKeep = 15;
        private int maxKpopKeep = 3;
        private long maxDurationMs = 2_400_000L;
        private List<String> ostMarkers = new ArrayList<>(List.of(
                "OST", "Original Television Soundtrack", "Original Soundtrack", "오리지널 사운드트랙"
        ));
        private List<String> albumDenyKeywords = new ArrayList<>(List.of(
                "piano", "cover", "tribute", "karaoke", "노래방", "live"
        ));
        private List<String> trackDenyKeywords = new ArrayList<>(List.of(
                "cover", "live", "karaoke", "노래방", "piano ver", "slowed", "nightcore", "8d"
        ));
    }
}
