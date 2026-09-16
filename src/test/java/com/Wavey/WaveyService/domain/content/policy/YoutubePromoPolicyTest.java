package com.Wavey.WaveyService.domain.content.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YoutubePromoPolicyTest {

    private YoutubePromoPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new YoutubePromoPolicy(new MediaCollectProperties());
    }

    @Test
    void 예고편은_통과한다() {
        assertThat(policy.shouldKeep(video("도깨비 메인 예고편", 90), ContentCategory.DRAMA, "도깨비", "Guardian", Set.of()))
                .isTrue();
    }

    @Test
    void 다시보기는_탈락한다() {
        assertThat(policy.shouldKeep(video("도깨비 1화 다시보기", 90), ContentCategory.DRAMA, "도깨비", null, Set.of()))
                .isFalse();
    }

    @Test
    void 본편_길이는_제목과_상관없이_탈락한다() {
        assertThat(policy.shouldKeep(video("도깨비 예고편", 70 * 60), ContentCategory.DRAMA, "도깨비", null, Set.of()))
                .isFalse();
    }

    @Test
    void 허용_단어가_없으면_탈락한다() {
        assertThat(policy.shouldKeep(video("도깨비 명장면 모음", 180), ContentCategory.DRAMA, "도깨비", null, Set.of()))
                .isFalse();
    }

    @Test
    void 숨긴_영상은_다시_살리지_않는다() {
        YoutubeVideoDetails video = video("도깨비 메인 예고편", 90);
        assertThat(policy.shouldKeep(video, ContentCategory.DRAMA, "도깨비", null, Set.of(video.videoId())))
                .isFalse();
    }

    @Test
    void 영문_제목만_있어도_통과한다() {
        assertThat(policy.shouldKeep(video("Guardian Official Teaser", 45), ContentCategory.DRAMA, "도깨비", "Guardian", Set.of()))
                .isTrue();
    }

    @Test
    void 문화유산_소개영상은_통과한다() {
        YoutubeVideoDetails video = new YoutubeVideoDetails(
                "heritage1",
                "[한국의 문화유산] 경복궁 근정전 소개영상",
                "국보 제223호, 경복궁의 중심이자 조선 왕실의 상징.",
                null,
                "국가유산채널(K-Heritage Channel)",
                360
        );

        assertThat(policy.shouldKeep(video, ContentCategory.HERITAGE, "경복궁 근정전", null, Set.of()))
                .isTrue();
    }

    @Test
    void 문화유산_브이로그는_탈락한다() {
        YoutubeVideoDetails video = new YoutubeVideoDetails(
                "heritage2",
                "경복궁 브이로그",
                "경복궁 맛집과 데이트 코스 소개",
                null,
                "여행로그",
                480
        );

        assertThat(policy.shouldKeep(video, ContentCategory.HERITAGE, "경복궁", null, Set.of()))
                .isFalse();
    }

    @Test
    void 문화유산은_콘텐츠_이름_일부만_있어도_통과한다() {
        YoutubeVideoDetails video = new YoutubeVideoDetails(
                "heritage5",
                "[한국의 문화유산] 경복궁 소개영상",
                "조선 왕실의 상징.",
                null,
                "국가유산채널(K-Heritage Channel)",
                360
        );

        assertThat(policy.shouldKeep(video, ContentCategory.HERITAGE, "경복궁 근정전", null, Set.of()))
                .isTrue();
    }

    private YoutubeVideoDetails video(String title, int durationSec) {
        return new YoutubeVideoDetails("GIAnKeKXzGU", title, null, "https://i.ytimg.com/vi/GIAnKeKXzGU/hqdefault.jpg", "tvN", durationSec);
    }
}
