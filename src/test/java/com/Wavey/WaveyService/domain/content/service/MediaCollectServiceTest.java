package com.Wavey.WaveyService.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.entity.WorkVideo;
import com.Wavey.WaveyService.domain.content.external.client.SpotifyApiClient;
import com.Wavey.WaveyService.domain.content.external.client.YoutubeDataClient;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifyAlbumTracks;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifySearchTrack;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.domain.content.policy.SpotifyOstPolicy;
import com.Wavey.WaveyService.domain.content.policy.YoutubePromoPolicy;
import com.Wavey.WaveyService.domain.content.repository.WorkTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.WorkVideoRepository;
import com.Wavey.WaveyService.domain.work.entity.Work;
import com.Wavey.WaveyService.domain.work.entity.WorkType;
import com.Wavey.WaveyService.domain.work.service.WorkService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MediaCollectServiceTest {

    @Mock
    private WorkService workService;
    @Mock
    private YoutubeDataClient youtubeDataClient;
    @Mock
    private SpotifyApiClient spotifyApiClient;
    @Mock
    private WorkVideoRepository workVideoRepository;
    @Mock
    private WorkTrackRepository workTrackRepository;

    private MediaCollectService mediaCollectService;

    @BeforeEach
    void setUp() {
        MediaCollectProperties properties = new MediaCollectProperties();
        mediaCollectService = new MediaCollectService(
                workService,
                youtubeDataClient,
                spotifyApiClient,
                new YoutubePromoPolicy(properties),
                new SpotifyOstPolicy(properties),
                workVideoRepository,
                workTrackRepository,
                properties
        );
    }

    @Test
    void 유튜브_수집은_예고편만_저장한다() {
        given(workService.getWork(1L)).willReturn(goblin());
        given(workVideoRepository.findByWorkId(1L)).willReturn(List.of());
        given(youtubeDataClient.searchVideoIds(anyString(), anyInt())).willReturn(List.of("promo111111", "full2222222"));
        given(youtubeDataClient.fetchVideos(any())).willReturn(List.of(
                youtube("promo111111", "도깨비 메인 예고편", 90),
                youtube("full2222222", "도깨비 1화 다시보기", 4200)
        ));
        given(workVideoRepository.findByWorkIdAndYoutubeVideoId(1L, "promo111111")).willReturn(Optional.empty());
        given(workVideoRepository.save(any(WorkVideo.class))).willAnswer(invocation -> {
            WorkVideo video = invocation.getArgument(0);
            ReflectionTestUtils.setField(video, "id", 10L);
            return video;
        });

        MediaCollectResponse response = mediaCollectService.refreshVideos(1L);

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getDropped()).isEqualTo(1);
        assertThat(response.getVideos()).hasSize(1);
        assertThat(response.getVideos().get(0).getTitle()).isEqualTo("도깨비 메인 예고편");
        verify(workVideoRepository).deleteByWorkIdAndHiddenFalseAndYoutubeVideoIdNotIn(eq(1L), anyCollection());
        verify(spotifyApiClient, never()).searchTracks(anyString(), anyInt());
    }

    @Test
    void 스포티파이_수집은_공식_OST_앨범_수록곡을_저장한다() {
        given(workService.getWork(1L)).willReturn(goblin());
        given(workTrackRepository.findByWorkId(1L)).willReturn(List.of());
        given(spotifyApiClient.searchTracks("도깨비 OST", 10)).willReturn(List.of(
                track("stay1", "Stay With Me", "album-1", "Guardian (Original Television Soundtrack), Pt. 1")
        ));
        given(spotifyApiClient.searchOstAlbums("Guardian Original Television Soundtrack", 10)).willReturn(List.of());
        given(spotifyApiClient.fetchAlbumTracks("album-1")).willReturn(new SpotifyAlbumTracks(
                "album-1",
                "Guardian (Original Television Soundtrack), Pt. 1",
                "https://i.scdn.co/image/cover",
                List.of(track("stay1", "Stay With Me", "album-1", "Guardian (Original Television Soundtrack), Pt. 1"))
        ));
        given(workTrackRepository.findByWorkIdAndSpotifyId(1L, "stay1")).willReturn(Optional.empty());
        given(workTrackRepository.save(any())).willAnswer(invocation -> {
            var track = invocation.getArgument(0);
            ReflectionTestUtils.setField(track, "id", 20L);
            return track;
        });

        MediaCollectResponse response = mediaCollectService.refreshTracks(1L);

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getTracks().get(0).getName()).isEqualTo("Stay With Me");
        assertThat(response.getTracks().get(0).isPreviewAvailable()).isFalse();
        verify(spotifyApiClient, never()).searchTracks(eq("도깨비 original soundtrack"), anyInt());
    }

    private Work goblin() {
        Work work = Work.builder()
                .title("도깨비")
                .titleEn("Guardian")
                .type(WorkType.DRAMA)
                .build();
        ReflectionTestUtils.setField(work, "id", 1L);
        return work;
    }

    private YoutubeVideoDetails youtube(String videoId, String title, int durationSec) {
        return new YoutubeVideoDetails(videoId, title, null, "https://i.ytimg.com/vi/" + videoId + "/hq.jpg", "tvN", durationSec);
    }

    private SpotifySearchTrack track(String id, String title, String albumId, String albumName) {
        return new SpotifySearchTrack(id, title, "CHANYEOL, Punch", albumId, albumName, "https://i.scdn.co/image/cover", 198000L, null, null);
    }
}
