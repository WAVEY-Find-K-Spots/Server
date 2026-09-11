package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentMediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentVideoResponse;
import com.Wavey.WaveyService.domain.content.entity.ContentTrack;
import com.Wavey.WaveyService.domain.content.entity.ContentVideo;
import com.Wavey.WaveyService.domain.content.external.client.SpotifyApiClient;
import com.Wavey.WaveyService.domain.content.external.client.YoutubeDataClient;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifyAlbumTracks;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifySearchTrack;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.domain.content.policy.SpotifyOstPolicy;
import com.Wavey.WaveyService.domain.content.policy.YoutubePromoPolicy;
import com.Wavey.WaveyService.domain.content.repository.ContentTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentVideoRepository;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.service.ContentService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MediaCollectService {

    private static final String YOUTUBE_THUMBNAIL_TEMPLATE = "https://i.ytimg.com/vi/%s/hqdefault.jpg";
    private static final String SPOTIFY_TRACK_URL_TEMPLATE = "https://open.spotify.com/track/%s";

    private final ContentService workService;
    private final YoutubeDataClient youtubeDataClient;
    private final SpotifyApiClient spotifyApiClient;
    private final YoutubePromoPolicy youtubePromoPolicy;
    private final SpotifyOstPolicy spotifyOstPolicy;
    private final ContentVideoRepository workVideoRepository;
    private final ContentTrackRepository workTrackRepository;
    private final MediaCollectProperties properties;

    @Transactional
    public ContentMediaCollectResponse collectAll(Long contentId) {
        return ContentMediaCollectResponse.builder()
                .contentId(contentId)
                .videos(refreshVideos(contentId))
                .tracks(refreshTracks(contentId))
                .build();
    }

    @Transactional
    public MediaCollectResponse refreshVideos(Long contentId) {
        Content work = workService.getContent(contentId);
        Set<String> hiddenIds = hiddenYoutubeIds(contentId);

        List<String> videoIds = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String query : youtubeQueries(work)) {
            for (String videoId : youtubeDataClient.searchVideoIds(query, 10)) {
                if (seen.add(videoId)) {
                    videoIds.add(videoId);
                }
            }
        }

        List<YoutubeVideoDetails> details = youtubeDataClient.fetchVideos(videoIds);
        int dropped = 0;
        List<YoutubeVideoDetails> kept = new ArrayList<>();
        for (YoutubeVideoDetails video : details) {
            if (youtubePromoPolicy.shouldKeep(video, work.getTitle(), work.getTitleEn(), hiddenIds)) {
                kept.add(video);
            } else {
                dropped++;
            }
        }

        int maxKeep = properties.getYoutube().getMaxKeep();
        if (kept.size() > maxKeep) {
            dropped += kept.size() - maxKeep;
            kept = kept.subList(0, maxKeep);
        }

        List<ContentVideo> saved = persistVideos(contentId, kept);
        return MediaCollectResponse.builder()
                .contentId(contentId)
                .saved(saved.size())
                .dropped(dropped)
                .videos(saved.stream().map(ContentVideoResponse::from).toList())
                .build();
    }

    @Transactional
    public MediaCollectResponse refreshTracks(Long contentId) {
        Content work = workService.getContent(contentId);
        Set<String> hiddenIds = hiddenSpotifyIds(contentId);

        TrackCollectResult collected = work.getCategory() == ContentCategory.ARTIST
                ? collectArtistTracks(work, hiddenIds)
                : collectOstTracks(work, hiddenIds);

        List<ContentTrack> saved = persistTracks(contentId, collected.kept());
        return MediaCollectResponse.builder()
                .contentId(contentId)
                .saved(saved.size())
                .dropped(collected.dropped())
                .tracks(saved.stream().map(ContentTrackResponse::from).toList())
                .build();
    }

    private TrackCollectResult collectOstTracks(Content work, Set<String> hiddenIds) {
        Map<String, SpotifySearchTrack> kept = new LinkedHashMap<>();
        int dropped = 0;

        List<SpotifySearchTrack> searched = spotifyApiClient.searchTracks(work.getTitle().trim() + " OST", 10);
        Set<String> albumIds = new LinkedHashSet<>();
        for (SpotifySearchTrack track : searched) {
            if (StringUtils.hasText(track.albumId()) && spotifyOstPolicy.shouldKeepAlbum(
                    track.albumName(), work.getTitle(), work.getTitleEn())) {
                albumIds.add(track.albumId());
            } else {
                dropped++;
            }
        }

        if (StringUtils.hasText(work.getTitleEn())) {
            for (SpotifyAlbumTracks album : spotifyApiClient.searchOstAlbums(
                    work.getTitleEn().trim() + " Original Television Soundtrack", 10)) {
                if (spotifyOstPolicy.shouldKeepAlbum(album.name(), work.getTitle(), work.getTitleEn())) {
                    albumIds.add(album.albumId());
                } else {
                    dropped++;
                }
            }
        }

        for (String albumId : albumIds) {
            SpotifyAlbumTracks album = spotifyApiClient.fetchAlbumTracks(albumId);
            if (album == null || album.tracks() == null) {
                continue;
            }
            for (SpotifySearchTrack track : album.tracks()) {
                if (hiddenIds.contains(track.trackId())
                        || !spotifyOstPolicy.shouldKeepAlbumTrack(track.title(), track.durationMs())) {
                    dropped++;
                    continue;
                }
                kept.putIfAbsent(track.trackId(), track);
                if (kept.size() >= properties.getSpotify().getMaxKeep()) {
                    return new TrackCollectResult(new ArrayList<>(kept.values()), dropped);
                }
            }
        }
        return new TrackCollectResult(new ArrayList<>(kept.values()), dropped);
    }

    private record TrackCollectResult(List<SpotifySearchTrack> kept, int dropped) {
    }

    private TrackCollectResult collectArtistTracks(Content work, Set<String> hiddenIds) {
        List<SpotifySearchTrack> searched = spotifyApiClient.searchTracks(work.getTitle().trim(), 10);
        List<SpotifySearchTrack> kept = searched.stream()
                .filter(track -> !hiddenIds.contains(track.trackId()))
                .limit(properties.getSpotify().getMaxKeep())
                .toList();
        int dropped = searched.size() - kept.size();
        return new TrackCollectResult(kept, dropped);
    }

    private List<ContentVideo> persistVideos(Long contentId, List<YoutubeVideoDetails> kept) {
        List<String> keepIds = kept.stream().map(YoutubeVideoDetails::videoId).toList();
        if (keepIds.isEmpty()) {
            workVideoRepository.deleteByContentIdAndHiddenFalse(contentId);
        } else {
            workVideoRepository.deleteByContentIdAndHiddenFalseAndYoutubeVideoIdNotIn(contentId, keepIds);
        }

        LocalDateTime now = LocalDateTime.now();
        List<ContentVideo> saved = new ArrayList<>();
        for (YoutubeVideoDetails video : kept) {
            String thumbnail = StringUtils.hasText(video.thumbnailUrl())
                    ? video.thumbnailUrl()
                    : YOUTUBE_THUMBNAIL_TEMPLATE.formatted(video.videoId());
            ContentVideo entity = workVideoRepository.findByContentIdAndYoutubeVideoId(contentId, video.videoId())
                    .orElseGet(() -> ContentVideo.builder()
                            .contentId(contentId)
                            .youtubeVideoId(video.videoId())
                            .title(video.title())
                            .channelTitle(video.channelTitle())
                            .thumbnailUrl(thumbnail)
                            .durationSec(video.durationSec())
                            .hidden(false)
                            .fetchedAt(now)
                            .build());
            if (entity.getContentVideoId() != null) {
                entity.updateFetched(video.title(), video.channelTitle(), thumbnail, video.durationSec());
            }
            saved.add(workVideoRepository.save(entity));
        }
        return saved;
    }

    private List<ContentTrack> persistTracks(Long contentId, List<SpotifySearchTrack> kept) {
        List<String> keepIds = kept.stream().map(SpotifySearchTrack::trackId).toList();
        if (keepIds.isEmpty()) {
            workTrackRepository.deleteByContentIdAndHiddenFalse(contentId);
        } else {
            workTrackRepository.deleteByContentIdAndHiddenFalseAndSpotifyTrackIdNotIn(contentId, keepIds);
        }

        LocalDateTime now = LocalDateTime.now();
        List<ContentTrack> saved = new ArrayList<>();
        for (SpotifySearchTrack track : kept) {
            String spotifyUrl = StringUtils.hasText(track.spotifyUrl())
                    ? track.spotifyUrl()
                    : SPOTIFY_TRACK_URL_TEMPLATE.formatted(track.trackId());
            ContentTrack entity = workTrackRepository.findByContentIdAndSpotifyTrackId(contentId, track.trackId())
                    .orElseGet(() -> ContentTrack.builder()
                            .contentId(contentId)
                            .contentAlbumId(null)
                            .spotifyTrackId(track.trackId())
                            .title(track.title())
                            .artistName(track.artistName())
                            .imageUrl(track.thumbnailUrl())
                            .spotifyUrl(spotifyUrl)
                            .durationMs(track.durationMs())
                            .hidden(false)
                            .fetchedAt(now)
                            .build());
            if (entity.getContentTrackId() != null) {
                entity.updateFetched(
                        null,
                        track.title(),
                        track.artistName(),
                        track.thumbnailUrl(),
                        spotifyUrl,
                        track.durationMs()
                );
            }
            saved.add(workTrackRepository.save(entity));
        }
        return saved;
    }

    private List<String> youtubeQueries(Content work) {
        String title = work.getTitle().trim();
        if (work.getCategory() == ContentCategory.ARTIST) {
            return List.of(title + " 뮤직비디오");
        }
        return List.of(title + " 예고편", title + " 티저", title + " 메이킹");
    }

    private Set<String> hiddenYoutubeIds(Long contentId) {
        return workVideoRepository.findByContentId(contentId).stream()
                .filter(ContentVideo::isHidden)
                .map(ContentVideo::getYoutubeVideoId)
                .collect(Collectors.toSet());
    }

    private Set<String> hiddenSpotifyIds(Long contentId) {
        return workTrackRepository.findByContentId(contentId).stream()
                .filter(ContentTrack::isHidden)
                .map(ContentTrack::getSpotifyTrackId)
                .collect(Collectors.toSet());
    }
}
