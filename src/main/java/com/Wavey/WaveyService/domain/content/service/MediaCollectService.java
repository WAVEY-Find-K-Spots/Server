package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.dto.MediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkMediaCollectResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.WorkVideoResponse;
import com.Wavey.WaveyService.domain.content.entity.WorkTrack;
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

    private final WorkService workService;
    private final YoutubeDataClient youtubeDataClient;
    private final SpotifyApiClient spotifyApiClient;
    private final YoutubePromoPolicy youtubePromoPolicy;
    private final SpotifyOstPolicy spotifyOstPolicy;
    private final WorkVideoRepository workVideoRepository;
    private final WorkTrackRepository workTrackRepository;
    private final MediaCollectProperties properties;

    @Transactional
    public WorkMediaCollectResponse collectAll(Long workId) {
        return WorkMediaCollectResponse.builder()
                .workId(workId)
                .videos(refreshVideos(workId))
                .tracks(refreshTracks(workId))
                .build();
    }

    @Transactional
    public MediaCollectResponse refreshVideos(Long workId) {
        Work work = workService.getWork(workId);
        Set<String> hiddenIds = hiddenYoutubeIds(workId);

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

        List<WorkVideo> saved = persistVideos(workId, kept);
        return MediaCollectResponse.builder()
                .workId(workId)
                .saved(saved.size())
                .dropped(dropped)
                .videos(saved.stream().map(WorkVideoResponse::from).toList())
                .build();
    }

    @Transactional
    public MediaCollectResponse refreshTracks(Long workId) {
        Work work = workService.getWork(workId);
        Set<String> hiddenIds = hiddenSpotifyIds(workId);

        TrackCollectResult collected = work.getType() == WorkType.KPOP
                ? collectKpopTracks(work, hiddenIds)
                : collectOstTracks(work, hiddenIds);

        List<WorkTrack> saved = persistTracks(workId, collected.kept());
        return MediaCollectResponse.builder()
                .workId(workId)
                .saved(saved.size())
                .dropped(collected.dropped())
                .tracks(saved.stream().map(WorkTrackResponse::from).toList())
                .build();
    }

    private TrackCollectResult collectOstTracks(Work work, Set<String> hiddenIds) {
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

    private TrackCollectResult collectKpopTracks(Work work, Set<String> hiddenIds) {
        String query = StringUtils.hasText(work.getArtistName())
                ? work.getArtistName().trim() + " " + work.getTitle().trim()
                : work.getTitle().trim();
        List<SpotifySearchTrack> searched = spotifyApiClient.searchTracks(query, 10);
        List<SpotifySearchTrack> kept = new ArrayList<>();
        int dropped = 0;
        for (SpotifySearchTrack track : searched) {
            if (hiddenIds.contains(track.trackId())
                    || !spotifyOstPolicy.shouldKeepKpopTrack(track.title(), track.albumName(), track.durationMs())) {
                dropped++;
                continue;
            }
            kept.add(track);
            if (kept.size() >= properties.getSpotify().getMaxKpopKeep()) {
                break;
            }
        }
        return new TrackCollectResult(kept, dropped);
    }

    private record TrackCollectResult(List<SpotifySearchTrack> kept, int dropped) {
    }

    private List<WorkVideo> persistVideos(Long workId, List<YoutubeVideoDetails> kept) {
        List<String> keepIds = kept.stream().map(YoutubeVideoDetails::videoId).toList();
        if (keepIds.isEmpty()) {
            workVideoRepository.deleteByWorkIdAndHiddenFalse(workId);
        } else {
            workVideoRepository.deleteByWorkIdAndHiddenFalseAndYoutubeVideoIdNotIn(workId, keepIds);
        }

        LocalDateTime now = LocalDateTime.now();
        List<WorkVideo> saved = new ArrayList<>();
        for (YoutubeVideoDetails video : kept) {
            String thumbnail = StringUtils.hasText(video.thumbnailUrl())
                    ? video.thumbnailUrl()
                    : YOUTUBE_THUMBNAIL_TEMPLATE.formatted(video.videoId());
            WorkVideo entity = workVideoRepository.findByWorkIdAndYoutubeVideoId(workId, video.videoId())
                    .orElseGet(() -> WorkVideo.builder()
                            .workId(workId)
                            .youtubeVideoId(video.videoId())
                            .title(video.title())
                            .channelTitle(video.channelTitle())
                            .thumbnailUrl(thumbnail)
                            .durationSec(video.durationSec())
                            .hidden(false)
                            .fetchedAt(now)
                            .build());
            if (entity.getWorkVideoId() != null) {
                entity.updateFetched(video.title(), video.channelTitle(), thumbnail, video.durationSec());
            }
            saved.add(workVideoRepository.save(entity));
        }
        return saved;
    }

    private List<WorkTrack> persistTracks(Long workId, List<SpotifySearchTrack> kept) {
        List<String> keepIds = kept.stream().map(SpotifySearchTrack::trackId).toList();
        if (keepIds.isEmpty()) {
            workTrackRepository.deleteByWorkIdAndHiddenFalse(workId);
        } else {
            workTrackRepository.deleteByWorkIdAndHiddenFalseAndSpotifyIdNotIn(workId, keepIds);
        }

        LocalDateTime now = LocalDateTime.now();
        List<WorkTrack> saved = new ArrayList<>();
        for (SpotifySearchTrack track : kept) {
            String spotifyUrl = StringUtils.hasText(track.spotifyUrl())
                    ? track.spotifyUrl()
                    : SPOTIFY_TRACK_URL_TEMPLATE.formatted(track.trackId());
            WorkTrack entity = workTrackRepository.findByWorkIdAndSpotifyId(workId, track.trackId())
                    .orElseGet(() -> WorkTrack.builder()
                            .workId(workId)
                            .spotifyId(track.trackId())
                            .name(track.title())
                            .artistName(track.artistName())
                            .albumName(track.albumName())
                            .imageUrl(track.thumbnailUrl())
                            .previewUrl(track.previewUrl())
                            .spotifyUrl(spotifyUrl)
                            .durationMs(track.durationMs())
                            .hidden(false)
                            .fetchedAt(now)
                            .build());
            if (entity.getWorkTrackId() != null) {
                entity.updateFetched(
                        track.title(),
                        track.artistName(),
                        track.albumName(),
                        track.thumbnailUrl(),
                        track.previewUrl(),
                        spotifyUrl,
                        track.durationMs()
                );
            }
            saved.add(workTrackRepository.save(entity));
        }
        return saved;
    }

    private List<String> youtubeQueries(Work work) {
        String title = work.getTitle().trim();
        if (work.getType() == WorkType.KPOP) {
            return List.of(title + " MV", title + " 비하인드", title + " 메이킹");
        }
        return List.of(title + " 예고편", title + " 티저", title + " 메이킹");
    }

    private Set<String> hiddenYoutubeIds(Long workId) {
        return workVideoRepository.findByWorkId(workId).stream()
                .filter(WorkVideo::isHidden)
                .map(WorkVideo::getYoutubeVideoId)
                .collect(Collectors.toSet());
    }

    private Set<String> hiddenSpotifyIds(Long workId) {
        return workTrackRepository.findByWorkId(workId).stream()
                .filter(WorkTrack::isHidden)
                .map(WorkTrack::getSpotifyId)
                .collect(Collectors.toSet());
    }
}
