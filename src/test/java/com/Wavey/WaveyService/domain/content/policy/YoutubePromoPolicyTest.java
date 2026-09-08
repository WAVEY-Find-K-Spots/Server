package com.Wavey.WaveyService.domain.content.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
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
        assertThat(policy.shouldKeep(video("도깨비 메인 예고편", 90), "도깨비", "Guardian", Set.of()))
                .isTrue();
    }

    @Test
    void 다시보기는_탈락한다() {
        assertThat(policy.shouldKeep(video("도깨비 1화 다시보기", 90), "도깨비", null, Set.of()))
                .isFalse();
    }

    @Test
    void 본편_길이는_제목과_상관없이_탈락한다() {
        assertThat(policy.shouldKeep(video("도깨비 예고편", 70 * 60), "도깨비", null, Set.of()))
                .isFalse();
    }

    @Test
    void 허용_단어가_없으면_탈락한다() {
        assertThat(policy.shouldKeep(video("도깨비 명장면 모음", 180), "도깨비", null, Set.of()))
                .isFalse();
    }

    @Test
    void 숨긴_영상은_다시_살리지_않는다() {
        YoutubeVideoDetails video = video("도깨비 메인 예고편", 90);
        assertThat(policy.shouldKeep(video, "도깨비", null, Set.of(video.videoId())))
                .isFalse();
    }

    @Test
    void 영문_제목만_있어도_통과한다() {
        assertThat(policy.shouldKeep(video("Guardian Official Teaser", 45), "도깨비", "Guardian", Set.of()))
                .isTrue();
    }

    private YoutubeVideoDetails video(String title, int durationSec) {
        return new YoutubeVideoDetails("GIAnKeKXzGU", title, null, "https://i.ytimg.com/vi/GIAnKeKXzGU/hqdefault.jpg", "tvN", durationSec);
    }
}
