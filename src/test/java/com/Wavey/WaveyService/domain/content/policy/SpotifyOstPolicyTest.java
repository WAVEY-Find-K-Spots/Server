package com.Wavey.WaveyService.domain.content.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpotifyOstPolicyTest {

    private SpotifyOstPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new SpotifyOstPolicy(new MediaCollectProperties());
    }

    @Test
    void 공식_OST_앨범은_통과한다() {
        assertThat(policy.shouldKeepAlbum("Guardian (Original Television Soundtrack), Pt. 1", "도깨비", "Guardian")).isTrue();
    }

    @Test
    void 피아노_커버_앨범은_탈락한다() {
        assertThat(policy.shouldKeepAlbum("Goblin OST / 도깨비 OST (Piano Version)", "도깨비", "Guardian")).isFalse();
    }

    @Test
    void OST_표시가_없는_앨범은_탈락한다() {
        assertThat(policy.shouldKeepAlbum("도깨비", "도깨비", "Guardian")).isFalse();
    }

    @Test
    void 작품명이_없는_모음집_OST는_탈락한다() {
        assertThat(policy.shouldKeepAlbum(
                "비오는날 듣기 좋은 첼로, 바이올린 (뉴에이지, 영화, 드라마, 애니메이션, OST)",
                "태양의 후예",
                "Descendants of the Sun"
        )).isFalse();
    }

    @Test
    void 수록곡_커버는_탈락하고_본곡은_통과한다() {
        assertThat(policy.shouldKeepAlbumTrack("Stay With Me", 198_000L)).isTrue();
        assertThat(policy.shouldKeepAlbumTrack("Stay With Me (Cover)", 198_000L)).isFalse();
    }

    @Test
    void 미리듣기가_없어도_곡_자체는_통과한다() {
        assertThat(policy.shouldKeepAlbumTrack("Beautiful", null)).isTrue();
    }

    @Test
    void KPOP_슬로우드_리믹스는_탈락한다() {
        assertThat(policy.shouldKeepKpopTrack("Dynamite - Slowed", "Dynamite", 199_000L)).isFalse();
        assertThat(policy.shouldKeepKpopTrack("Dynamite", "Dynamite", 199_000L)).isTrue();
    }
}
