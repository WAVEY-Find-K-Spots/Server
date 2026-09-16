package com.Wavey.WaveyService.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentAlbum;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.entity.ContentVideo;
import com.Wavey.WaveyService.domain.content.external.client.SpotifyApiClient;
import com.Wavey.WaveyService.domain.content.external.client.YoutubeDataClient;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifyAlbumTracks;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifySearchTrack;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.domain.content.policy.SpotifyOstPolicy;
import com.Wavey.WaveyService.domain.content.policy.YoutubePromoPolicy;
import com.Wavey.WaveyService.domain.content.repository.ContentAlbumRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentVideoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MediaCollectServiceTest {

    @Mock
    private ContentService contentService;
    @Mock
    private YoutubeDataClient youtubeDataClient;
    @Mock
    private SpotifyApiClient spotifyApiClient;
    @Mock
    private ContentVideoRepository contentVideoRepository;
    @Mock
    private ContentAlbumRepository contentAlbumRepository;
    @Mock
    private ContentTrackRepository contentTrackRepository;

    private MediaCollectService mediaCollectService;

    @BeforeEach
    void setUp() {
        MediaCollectProperties properties = new MediaCollectProperties();
        mediaCollectService = new MediaCollectService(
                contentService,
                youtubeDataClient,
                spotifyApiClient,
                new YoutubePromoPolicy(properties),
                new SpotifyOstPolicy(properties),
                contentVideoRepository,
                contentAlbumRepository,
                contentTrackRepository,
                properties
        );
    }

    @Test
    void 유튜브_수집은_예고편만_저장한다() {
        given(contentService.getContent(1L)).willReturn(goblin());
        given(contentVideoRepository.findByContentId(1L)).willReturn(List.of());
        given(youtubeDataClient.searchVideoIds(anyString(), anyInt())).willReturn(List.of("promo111111", "full2222222"));
        given(youtubeDataClient.fetchVideos(any())).willReturn(List.of(
                youtube("promo111111", "도깨비 메인 예고편", 90),
                youtube("full2222222", "도깨비 1화 다시보기", 4200)
        ));
        given(contentVideoRepository.findByContentIdAndYoutubeVideoId(1L, "promo111111")).willReturn(Optional.empty());
        given(contentVideoRepository.save(any(ContentVideo.class))).willAnswer(invocation -> {
            ContentVideo video = invocation.getArgument(0);
            ReflectionTestUtils.setField(video, "id", 10L);
            return video;
        });

        MediaCollectResponse response = mediaCollectService.refreshVideos(1L);

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getDropped()).isEqualTo(1);
        assertThat(response.getVideos()).hasSize(1);
        assertThat(response.getVideos().get(0).getTitle()).isEqualTo("도깨비 메인 예고편");
        verify(contentVideoRepository).deleteByContentIdAndHiddenFalseAndYoutubeVideoIdNotIn(eq(1L), anyCollection());
        verify(spotifyApiClient, never()).searchTracks(anyString(), anyInt());
        verify(youtubeDataClient, never()).searchVideoIds(eq("도깨비 메이킹"), anyInt());
    }

    @Test
    void 유튜브_수집은_예고편_티저를_한_검색어로_조회한다() {
        given(contentService.getContent(1L)).willReturn(goblin());
        given(contentVideoRepository.findByContentId(1L)).willReturn(List.of());
        given(contentVideoRepository.findByContentIdAndHiddenFalseOrderByIdAsc(1L)).willReturn(List.of());

        List<String> ids = IntStream.rangeClosed(1, 8)
                .mapToObj(i -> "promo%08d".formatted(i))
                .toList();
        given(youtubeDataClient.searchVideoIds(eq("도깨비 (예고편 OR 티저)"), anyInt())).willReturn(ids);
        given(youtubeDataClient.fetchVideos(ids)).willReturn(
                ids.stream().map(id -> youtube(id, "도깨비 메인 예고편", 90)).toList()
        );
        given(contentVideoRepository.findByContentIdAndYoutubeVideoId(eq(1L), anyString())).willReturn(Optional.empty());
        given(contentVideoRepository.save(any(ContentVideo.class))).willAnswer(invocation -> {
            ContentVideo video = invocation.getArgument(0);
            ReflectionTestUtils.setField(video, "id", 10L);
            return video;
        });

        MediaCollectResponse response = mediaCollectService.refreshVideos(1L);

        assertThat(response.getSaved()).isEqualTo(8);
        verify(youtubeDataClient, times(1)).searchVideoIds(anyString(), anyInt());
        verify(youtubeDataClient).searchVideoIds("도깨비 (예고편 OR 티저)", 10);
        verify(youtubeDataClient, never()).searchVideoIds(eq("도깨비 예고편"), anyInt());
        verify(youtubeDataClient, never()).searchVideoIds(eq("도깨비 티저"), anyInt());
    }

    @Test
    void 유튜브_재수집은_캐시_유효하면_API를_생략한다() {
        ContentVideo existing = ContentVideo.builder()
                .contentId(1L)
                .youtubeVideoId("promo111111")
                .title("도깨비 메인 예고편")
                .channelTitle("tvN")
                .thumbnailUrl("https://img")
                .durationSec(90)
                .hidden(false)
                .fetchedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(existing, "id", 10L);

        given(contentService.getContent(1L)).willReturn(goblin());
        given(contentVideoRepository.findByContentIdAndHiddenFalseOrderByIdAsc(1L)).willReturn(List.of(existing));

        MediaCollectResponse response = mediaCollectService.refreshVideos(1L);

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getVideos()).hasSize(1);
        assertThat(response.getVideos().get(0).getVideoId()).isEqualTo("promo111111");
        verify(youtubeDataClient, never()).searchVideoIds(anyString(), anyInt());
        verify(youtubeDataClient, never()).fetchVideos(any());
        verify(contentVideoRepository, never()).save(any());
    }

    @Test
    void 문화유산_수집은_한_검색어로_조회한다() {
        Content heritage = Content.builder()
                .titleKo("경복궁 근정전")
                .titleEn("Geunjeongjeon")
                .category(ContentCategory.HERITAGE)
                .build();
        ReflectionTestUtils.setField(heritage, "id", 5L);

        List<String> ids = List.of("h1", "h2", "h3");
        given(contentService.getContent(5L)).willReturn(heritage);
        given(contentVideoRepository.findByContentId(5L)).willReturn(List.of());
        given(contentVideoRepository.findByContentIdAndHiddenFalseOrderByIdAsc(5L)).willReturn(List.of());
        given(youtubeDataClient.searchVideoIds(eq("경복궁 근정전 (소개영상 OR 문화유산 OR 국가유산 OR unesco)"), anyInt())).willReturn(ids);
        given(youtubeDataClient.fetchVideos(ids)).willReturn(List.of(
                heritageVideo("h1", "경복궁 근정전 소개영상", "국가유산채널"),
                heritageVideo("h2", "경복궁 근정전 문화유산 소개", "문화재청"),
                heritageVideo("h3", "경복궁 근정전 국가유산", "K-Heritage")
        ));
        given(contentVideoRepository.findByContentIdAndYoutubeVideoId(eq(5L), anyString())).willReturn(Optional.empty());
        given(contentVideoRepository.save(any(ContentVideo.class))).willAnswer(invocation -> {
            ContentVideo video = invocation.getArgument(0);
            ReflectionTestUtils.setField(video, "id", 50L);
            return video;
        });

        MediaCollectResponse response = mediaCollectService.refreshVideos(5L);

        assertThat(response.getSaved()).isEqualTo(3);
        verify(youtubeDataClient, times(1)).searchVideoIds(anyString(), anyInt());
        verify(youtubeDataClient).searchVideoIds("경복궁 근정전 (소개영상 OR 문화유산 OR 국가유산 OR unesco)", 10);
        verify(youtubeDataClient, never()).searchVideoIds(eq("경복궁 근정전 소개영상"), anyInt());
    }

    @Test
    void 문화유산_수집은_검색_순서대로_저장한다() {
        Content heritage = Content.builder()
                .titleKo("경복궁 근정전")
                .titleEn("Geunjeongjeon")
                .category(ContentCategory.HERITAGE)
                .build();
        ReflectionTestUtils.setField(heritage, "id", 3L);

        given(contentService.getContent(3L)).willReturn(heritage);
        given(contentVideoRepository.findByContentId(3L)).willReturn(List.of());
        given(youtubeDataClient.searchVideoIds(anyString(), anyInt())).willReturn(List.of("generic", "official"));
        given(youtubeDataClient.fetchVideos(any())).willReturn(List.of(
                new YoutubeVideoDetails(
                        "generic",
                        "경복궁 근정전 문화유산 소개",
                        "서울 문화유산 소개",
                        null,
                        "여행백과",
                        360
                ),
                new YoutubeVideoDetails(
                        "official",
                        "[한국의 문화유산] 경복궁 근정전 소개영상",
                        "국보 제223호, 경복궁의 중심이자 조선 왕실의 상징.",
                        null,
                        "국가유산채널(K-Heritage Channel)",
                        360
                )
        ));
        given(contentVideoRepository.findByContentIdAndYoutubeVideoId(eq(3L), anyString())).willReturn(Optional.empty());
        given(contentVideoRepository.save(any(ContentVideo.class))).willAnswer(invocation -> {
            ContentVideo video = invocation.getArgument(0);
            ReflectionTestUtils.setField(video, "id", 11L);
            return video;
        });

        MediaCollectResponse response = mediaCollectService.refreshVideos(3L);

        assertThat(response.getSaved()).isEqualTo(2);
        assertThat(response.getVideos()).hasSize(2);
        assertThat(response.getVideos().get(0).getVideoId()).isEqualTo("generic");
        assertThat(response.getVideos().get(1).getVideoId()).isEqualTo("official");
    }

    @Test
    void 문화유산은_스포티파이_수집을_건너뛴다() {
        Content heritage = Content.builder()
                .titleKo("경복궁 근정전")
                .category(ContentCategory.HERITAGE)
                .build();
        ReflectionTestUtils.setField(heritage, "id", 4L);

        given(contentService.getContent(4L)).willReturn(heritage);

        MediaCollectResponse response = mediaCollectService.refreshTracks(4L);

        assertThat(response.getSaved()).isZero();
        assertThat(response.getDropped()).isZero();
        assertThat(response.getAlbums()).isEmpty();
        assertThat(response.getTracks()).isEmpty();
        verify(spotifyApiClient, never()).searchTracks(anyString(), anyInt());
        verify(spotifyApiClient, never()).searchOstAlbums(anyString(), anyInt());
        verify(spotifyApiClient, never()).fetchAlbumTracks(anyString());
    }

    @Test
    void 스포티파이_수집은_공식_OST_앨범과_수록곡을_저장한다() {
        given(contentService.getContent(1L)).willReturn(musicWork());
        given(contentTrackRepository.findByContentId(1L)).willReturn(List.of());
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
        given(contentAlbumRepository.findByContentIdAndSpotifyAlbumId(1L, "album-1")).willReturn(Optional.empty());
        given(contentAlbumRepository.save(any(ContentAlbum.class))).willAnswer(invocation -> {
            ContentAlbum album = invocation.getArgument(0);
            ReflectionTestUtils.setField(album, "id", 5L);
            return album;
        });
        given(contentTrackRepository.findByContentIdAndSpotifyTrackId(1L, "stay1")).willReturn(Optional.empty());
        given(contentTrackRepository.save(any())).willAnswer(invocation -> {
            var track = invocation.getArgument(0);
            ReflectionTestUtils.setField(track, "id", 20L);
            return track;
        });

        MediaCollectResponse response = mediaCollectService.refreshTracks(1L);

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getAlbums()).hasSize(1);
        assertThat(response.getAlbums().get(0).getSpotifyAlbumId()).isEqualTo("album-1");
        assertThat(response.getTracks().get(0).getTitle()).isEqualTo("Stay With Me");
        assertThat(response.getTracks().get(0).getSpotifyTrackId()).isEqualTo("stay1");
        assertThat(response.getTracks().get(0).getContentAlbumId()).isEqualTo(5L);
        verify(contentAlbumRepository).save(any(ContentAlbum.class));
        verify(spotifyApiClient, never()).searchTracks(eq("도깨비 original soundtrack"), anyInt());
    }

    @Test
    void 아티스트_수집은_앨범_없이_단독_트랙만_저장한다() {
        Content artist = Content.builder()
                .titleKo("아이유")
                .titleEn("IU")
                .category(ContentCategory.ARTIST)
                .build();
        ReflectionTestUtils.setField(artist, "id", 2L);

        given(contentService.getContent(2L)).willReturn(artist);
        given(contentTrackRepository.findByContentId(2L)).willReturn(List.of());
        given(spotifyApiClient.searchTracks("아이유", 10)).willReturn(List.of(
                track("iu1", "밤편지", null, null)
        ));
        given(contentTrackRepository.findByContentIdAndSpotifyTrackId(2L, "iu1")).willReturn(Optional.empty());
        given(contentTrackRepository.save(any())).willAnswer(invocation -> {
            var saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 30L);
            return saved;
        });

        MediaCollectResponse response = mediaCollectService.refreshTracks(2L);

        assertThat(response.getSaved()).isEqualTo(1);
        assertThat(response.getAlbums()).isEmpty();
        assertThat(response.getTracks()).hasSize(1);
        assertThat(response.getTracks().get(0).getSpotifyTrackId()).isEqualTo("iu1");
        assertThat(response.getTracks().get(0).getContentAlbumId()).isNull();
        verify(contentAlbumRepository).deleteByContentIdAndHiddenFalse(2L);
        verify(contentAlbumRepository, never()).save(any());
        verify(spotifyApiClient, never()).fetchAlbumTracks(anyString());
    }

    private Content goblin() {
        Content content = Content.builder()
                .titleKo("도깨비")
                .titleEn("Guardian")
                .category(ContentCategory.DRAMA)
                .build();
        ReflectionTestUtils.setField(content, "id", 1L);
        return content;
    }

    private Content musicWork() {
        Content content = Content.builder()
                .titleKo("도깨비")
                .titleEn("Guardian")
                .category(ContentCategory.DRAMA)
                .build();
        ReflectionTestUtils.setField(content, "id", 1L);
        return content;
    }

    private YoutubeVideoDetails youtube(String videoId, String title, int durationSec) {
        return new YoutubeVideoDetails(videoId, title, null, "https://i.ytimg.com/vi/" + videoId + "/hq.jpg", "tvN", durationSec);
    }

    private YoutubeVideoDetails heritageVideo(String videoId, String title, String channelTitle) {
        return new YoutubeVideoDetails(
                videoId,
                title,
                "국보 제223호",
                "https://i.ytimg.com/vi/" + videoId + "/hq.jpg",
                channelTitle,
                360
        );
    }

    private SpotifySearchTrack track(String id, String title, String albumId, String albumName) {
        return new SpotifySearchTrack(id, title, "CHANYEOL, Punch", albumId, albumName, "https://i.scdn.co/image/cover", 198000L, null, null);
    }
}
