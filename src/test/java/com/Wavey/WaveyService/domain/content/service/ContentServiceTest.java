package com.Wavey.WaveyService.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentPlatform;
import com.Wavey.WaveyService.domain.content.external.client.SpotifyApiClient;
import com.Wavey.WaveyService.domain.content.external.client.YoutubeDataClient;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifyTrackDetails;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    private static final String YOUTUBE_VIDEO_ID = "GIAnKeKXzGU";
    private static final String OTHER_YOUTUBE_VIDEO_ID = "dQw4w9WgXcQ";
    private static final String SPOTIFY_TRACK_ID = "3n3Ppam7vgaVa1iaRUc9Lp";
    private static final String OTHER_SPOTIFY_TRACK_ID = "4uLU6hMCjMI75M1A2tKUQC";

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private YoutubeDataClient youtubeDataClient;

    @Mock
    private SpotifyApiClient spotifyApiClient;

    @InjectMocks
    private ContentService contentService;

    @Test
    void 유튜브_등록시_제목이_없으면_Data_API_제목을_저장한다() {
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.YOUTUBE, YOUTUBE_VIDEO_ID))
                .willReturn(Optional.empty());
        given(youtubeDataClient.fetchVideo(YOUTUBE_VIDEO_ID)).willReturn(youtubeDetails(
                YOUTUBE_VIDEO_ID, "도깨비 하이라이트", "tvN 하이라이트"));
        given(contentRepository.save(any(Content.class))).willAnswer(invocation -> {
            Content content = invocation.getArgument(0);
            ReflectionTestUtils.setField(content, "id", 1L);
            return content;
        });

        ContentResponse response = contentService.createYoutube(youtubeRequest(
                "https://www.youtube.com/watch?v=" + YOUTUBE_VIDEO_ID));

        assertThat(response.getContentId()).isEqualTo(1L);
        assertThat(response.getPlatform()).isEqualTo(ContentPlatform.YOUTUBE);
        assertThat(response.getTitle()).isEqualTo("도깨비 하이라이트");
        assertThat(response.getDescription()).isEqualTo("tvN 하이라이트");
        assertThat(response.getExternalId()).isEqualTo(YOUTUBE_VIDEO_ID);
        assertThat(response.getThumbnailUrl()).isEqualTo("https://i.ytimg.com/vi/" + YOUTUBE_VIDEO_ID + "/hqdefault.jpg");
    }

    @Test
    void 스포티파이_등록시_제목이_없으면_Web_API_제목과_아티스트를_저장한다() {
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.SPOTIFY, SPOTIFY_TRACK_ID))
                .willReturn(Optional.empty());
        given(spotifyApiClient.fetchTrack(SPOTIFY_TRACK_ID)).willReturn(spotifyDetails(
                SPOTIFY_TRACK_ID, "Stay With Me", "CHANYEOL, Punch", "도깨비 OST Part 1"));
        given(contentRepository.save(any(Content.class))).willAnswer(invocation -> {
            Content content = invocation.getArgument(0);
            ReflectionTestUtils.setField(content, "id", 2L);
            return content;
        });

        ContentResponse response = contentService.createSpotify(spotifyRequest(
                "https://open.spotify.com/track/" + SPOTIFY_TRACK_ID));

        assertThat(response.getContentId()).isEqualTo(2L);
        assertThat(response.getPlatform()).isEqualTo(ContentPlatform.SPOTIFY);
        assertThat(response.getTitle()).isEqualTo("Stay With Me");
        assertThat(response.getDescription()).isEqualTo("CHANYEOL, Punch · 도깨비 OST Part 1");
        assertThat(response.getExternalId()).isEqualTo(SPOTIFY_TRACK_ID);
        assertThat(response.getThumbnailUrl()).isEqualTo("https://i.scdn.co/image/" + SPOTIFY_TRACK_ID);
    }

    @Test
    void 유튜브가_아닌_URL이면_등록을_거절한다() {
        assertThatThrownBy(() -> contentService.createYoutube(
                youtubeRequest("https://www.naver.com/watch?v=" + YOUTUBE_VIDEO_ID)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_YOUTUBE_URL);

        verifyNoInteractions(youtubeDataClient, contentRepository);
    }

    @Test
    void 유튜브_영상_ID가_형식이_아니면_등록을_거절한다() {
        assertThatThrownBy(() -> contentService.createYoutube(
                youtubeRequest("https://www.youtube.com/watch?v=short")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_YOUTUBE_URL);

        verifyNoInteractions(youtubeDataClient, contentRepository);
    }

    @Test
    void 스포티파이_트랙이_아닌_URL이면_등록을_거절한다() {
        assertThatThrownBy(() -> contentService.createSpotify(
                spotifyRequest("https://open.spotify.com/album/" + SPOTIFY_TRACK_ID)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SPOTIFY_URL);

        verifyNoInteractions(spotifyApiClient, contentRepository);
    }

    @Test
    void URL이_비어_있으면_등록을_거절한다() {
        assertThatThrownBy(() -> contentService.createYoutube(youtubeRequest("  ")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_URL_REQUIRED);

        verifyNoInteractions(youtubeDataClient, contentRepository);
    }

    @Test
    void 이미_등록된_유튜브_영상이면_중복_예외가_발생한다() {
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.YOUTUBE, YOUTUBE_VIDEO_ID))
                .willReturn(Optional.of(youtubeContent(1L, YOUTUBE_VIDEO_ID, "기존 제목")));

        assertThatThrownBy(() -> contentService.createYoutube(
                youtubeRequest("https://www.youtube.com/watch?v=" + YOUTUBE_VIDEO_ID)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_ALREADY_EXISTS);

        verify(youtubeDataClient, never()).fetchVideo(any());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void 이미_등록된_스포티파이_트랙이면_중복_예외가_발생한다() {
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.SPOTIFY, SPOTIFY_TRACK_ID))
                .willReturn(Optional.of(spotifyContent(2L, SPOTIFY_TRACK_ID, "기존 곡")));

        assertThatThrownBy(() -> contentService.createSpotify(
                spotifyRequest("https://open.spotify.com/track/" + SPOTIFY_TRACK_ID)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_ALREADY_EXISTS);

        verify(spotifyApiClient, never()).fetchTrack(any());
        verify(contentRepository, never()).save(any());
    }

    @Test
    void 유튜브_수정시_API에서_받은_제목으로_갱신한다() {
        Content content = youtubeContent(1L, YOUTUBE_VIDEO_ID, "예전 제목");
        given(contentRepository.findById(1L)).willReturn(Optional.of(content));
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.YOUTUBE, OTHER_YOUTUBE_VIDEO_ID))
                .willReturn(Optional.empty());
        given(youtubeDataClient.fetchVideo(OTHER_YOUTUBE_VIDEO_ID)).willReturn(youtubeDetails(
                OTHER_YOUTUBE_VIDEO_ID, "새 하이라이트", "새 설명"));

        ContentResponse response = contentService.updateYoutube(1L, youtubeRequest(
                "https://youtu.be/" + OTHER_YOUTUBE_VIDEO_ID));

        assertThat(response.getTitle()).isEqualTo("새 하이라이트");
        assertThat(response.getDescription()).isEqualTo("새 설명");
        assertThat(response.getExternalId()).isEqualTo(OTHER_YOUTUBE_VIDEO_ID);
        assertThat(content.getTitle()).isEqualTo("새 하이라이트");
        assertThat(content.getExternalId()).isEqualTo(OTHER_YOUTUBE_VIDEO_ID);
    }

    @Test
    void 유튜브_수정시_다른_콘텐츠가_쓰는_영상이면_중복_예외가_발생한다() {
        given(contentRepository.findById(1L))
                .willReturn(Optional.of(youtubeContent(1L, YOUTUBE_VIDEO_ID, "내 영상")));
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.YOUTUBE, OTHER_YOUTUBE_VIDEO_ID))
                .willReturn(Optional.of(youtubeContent(9L, OTHER_YOUTUBE_VIDEO_ID, "남의 영상")));

        assertThatThrownBy(() -> contentService.updateYoutube(1L, youtubeRequest(
                "https://www.youtube.com/watch?v=" + OTHER_YOUTUBE_VIDEO_ID)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_ALREADY_EXISTS);

        verify(youtubeDataClient, never()).fetchVideo(any());
    }

    @Test
    void 스포티파이_수정시_API에서_받은_제목으로_갱신한다() {
        Content content = spotifyContent(2L, SPOTIFY_TRACK_ID, "예전 곡");
        given(contentRepository.findById(2L)).willReturn(Optional.of(content));
        given(contentRepository.findByPlatformAndExternalId(ContentPlatform.SPOTIFY, OTHER_SPOTIFY_TRACK_ID))
                .willReturn(Optional.empty());
        given(spotifyApiClient.fetchTrack(OTHER_SPOTIFY_TRACK_ID)).willReturn(spotifyDetails(
                OTHER_SPOTIFY_TRACK_ID, "Beautiful", "Crush", "도깨비 OST Part 4"));

        ContentResponse response = contentService.updateSpotify(2L, spotifyRequest(
                "https://open.spotify.com/track/" + OTHER_SPOTIFY_TRACK_ID));

        assertThat(response.getTitle()).isEqualTo("Beautiful");
        assertThat(response.getDescription()).isEqualTo("Crush · 도깨비 OST Part 4");
        assertThat(response.getExternalId()).isEqualTo(OTHER_SPOTIFY_TRACK_ID);
        assertThat(content.getTitle()).isEqualTo("Beautiful");
    }

    @Test
    void 유튜브_콘텐츠를_삭제한다() {
        Content content = youtubeContent(1L, YOUTUBE_VIDEO_ID, "도깨비 하이라이트");
        given(contentRepository.findById(1L)).willReturn(Optional.of(content));

        contentService.deleteYoutube(1L);

        verify(contentRepository).delete(content);
    }

    @Test
    void 스포티파이_콘텐츠를_삭제한다() {
        Content content = spotifyContent(2L, SPOTIFY_TRACK_ID, "Stay With Me");
        given(contentRepository.findById(2L)).willReturn(Optional.of(content));

        contentService.deleteSpotify(2L);

        verify(contentRepository).delete(content);
    }

    @Test
    void 없는_콘텐츠를_삭제하면_예외가_발생한다() {
        given(contentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> contentService.deleteYoutube(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_NOT_FOUND);

        verify(contentRepository, never()).delete(any());
    }

    @Test
    void 스포티파이_콘텐츠를_유튜브_삭제로_지우면_예외가_발생한다() {
        given(contentRepository.findById(2L))
                .willReturn(Optional.of(spotifyContent(2L, SPOTIFY_TRACK_ID, "Stay With Me")));

        assertThatThrownBy(() -> contentService.deleteYoutube(2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONTENT_NOT_FOUND);

        verify(contentRepository, never()).delete(any());
    }

    private ContentRequest youtubeRequest(String url) {
        return ContentRequest.builder().url(url).build();
    }

    private ContentRequest spotifyRequest(String url) {
        return ContentRequest.builder().url(url).build();
    }

    private YoutubeVideoDetails youtubeDetails(String videoId, String title, String description) {
        return new YoutubeVideoDetails(
                videoId,
                title,
                description,
                "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg",
                "tvN DRAMA",
                90
        );
    }

    private SpotifyTrackDetails spotifyDetails(String trackId, String title, String artist, String album) {
        return new SpotifyTrackDetails(
                trackId,
                title,
                artist,
                album,
                "https://i.scdn.co/image/" + trackId
        );
    }

    private Content youtubeContent(Long id, String externalId, String title) {
        Content content = Content.builder()
                .platform(ContentPlatform.YOUTUBE)
                .title(title)
                .description("기존 설명")
                .externalId(externalId)
                .thumbnailUrl("https://i.ytimg.com/vi/" + externalId + "/hqdefault.jpg")
                .build();
        ReflectionTestUtils.setField(content, "id", id);
        return content;
    }

    private Content spotifyContent(Long id, String externalId, String title) {
        Content content = Content.builder()
                .platform(ContentPlatform.SPOTIFY)
                .title(title)
                .description("기존 설명")
                .externalId(externalId)
                .thumbnailUrl("https://i.scdn.co/image/" + externalId)
                .build();
        ReflectionTestUtils.setField(content, "id", id);
        return content;
    }
}
