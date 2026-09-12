package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.dto.ContentAlbumResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentMediaBundleResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentTrackResponse;
import com.Wavey.WaveyService.domain.content.dto.ContentVideoResponse;
import com.Wavey.WaveyService.domain.content.dto.SpotMediaResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentAlbum;
import com.Wavey.WaveyService.domain.content.entity.ContentTrack;
import com.Wavey.WaveyService.domain.content.entity.ContentVideo;
import com.Wavey.WaveyService.domain.content.entity.SpotContent;
import com.Wavey.WaveyService.domain.content.repository.ContentAlbumRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentVideoRepository;
import com.Wavey.WaveyService.domain.content.repository.SpotContentRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentMediaQueryService {

    private final ContentService workService;
    private final ContentRepository contentRepository;
    private final SpotRepository spotRepository;
    private final SpotContentRepository spotContentRepository;
    private final ContentVideoRepository workVideoRepository;
    private final ContentAlbumRepository contentAlbumRepository;
    private final ContentTrackRepository workTrackRepository;

    @Transactional(readOnly = true)
    public List<ContentVideoResponse> listVideos(Long contentId) {
        workService.getContent(contentId);
        return workVideoRepository.findByContentIdAndHiddenFalseOrderByIdAsc(contentId).stream()
                .map(ContentVideoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentAlbumResponse> listAlbums(Long contentId) {
        workService.getContent(contentId);
        List<ContentAlbum> albums = contentAlbumRepository.findByContentIdAndHiddenFalseOrderByIdAsc(contentId);
        return toAlbumResponses(albums);
    }

    @Transactional(readOnly = true)
    public List<ContentTrackResponse> listStandaloneTracks(Long contentId) {
        workService.getContent(contentId);
        return workTrackRepository.findByContentIdAndContentAlbumIdIsNullAndHiddenFalseOrderByIdAsc(contentId).stream()
                .map(ContentTrackResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentTrackResponse> listAlbumTracks(Long contentAlbumId) {
        ContentAlbum album = contentAlbumRepository.findById(contentAlbumId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_ALBUM_NOT_FOUND));
        if (album.isHidden()) {
            throw new CustomException(ErrorCode.CONTENT_ALBUM_NOT_FOUND);
        }
        return workTrackRepository.findByContentAlbumIdAndHiddenFalseOrderByIdAsc(contentAlbumId).stream()
                .map(ContentTrackResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpotMediaResponse getSpotMedia(Long spotId) {
        if (!spotRepository.existsById(spotId)) {
            throw new CustomException(ErrorCode.SPOT_NOT_FOUND);
        }

        List<SpotContent> links = spotContentRepository.findBySpotIdOrderByIdAsc(spotId);
        List<Long> contentIds = links.stream().map(SpotContent::getContentId).distinct().toList();
        if (contentIds.isEmpty()) {
            return SpotMediaResponse.builder().spotId(spotId).contents(List.of()).build();
        }

        Map<Long, Content> contentById = contentRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(Content::getContentId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

        Map<Long, List<ContentVideo>> videosByContentId = workVideoRepository
                .findByContentIdInAndHiddenFalseOrderByIdAsc(contentIds).stream()
                .collect(Collectors.groupingBy(ContentVideo::getContentId, LinkedHashMap::new, Collectors.toList()));

        List<ContentAlbum> allAlbums = contentAlbumRepository
                .findByContentIdInAndHiddenFalseOrderByIdAsc(contentIds);
        Map<Long, List<ContentAlbum>> albumsByContentId = allAlbums.stream()
                .collect(Collectors.groupingBy(ContentAlbum::getContentId, LinkedHashMap::new, Collectors.toList()));

        List<Long> albumIds = allAlbums.stream().map(ContentAlbum::getContentAlbumId).toList();
        Map<Long, List<ContentTrack>> tracksByAlbumId = albumIds.isEmpty()
                ? Map.of()
                : workTrackRepository.findByContentAlbumIdInAndHiddenFalseOrderByIdAsc(albumIds).stream()
                .collect(Collectors.groupingBy(ContentTrack::getContentAlbumId, LinkedHashMap::new, Collectors.toList()));

        Map<Long, List<ContentTrack>> standaloneTracksByContentId = workTrackRepository
                .findByContentIdInAndContentAlbumIdIsNullAndHiddenFalseOrderByIdAsc(contentIds).stream()
                .collect(Collectors.groupingBy(ContentTrack::getContentId, LinkedHashMap::new, Collectors.toList()));

        List<ContentMediaBundleResponse> bundles = new ArrayList<>();
        for (Long contentId : contentIds) {
            Content content = contentById.get(contentId);
            if (content == null) {
                continue;
            }
            List<ContentAlbumResponse> albums = albumsByContentId.getOrDefault(contentId, List.of()).stream()
                    .map(album -> ContentAlbumResponse.from(
                            album,
                            tracksByAlbumId.getOrDefault(album.getContentAlbumId(), List.of()).stream()
                                    .map(ContentTrackResponse::from)
                                    .toList()
                    ))
                    .toList();
            List<ContentTrackResponse> tracks = standaloneTracksByContentId
                    .getOrDefault(contentId, List.of()).stream()
                    .map(ContentTrackResponse::from)
                    .toList();

            bundles.add(ContentMediaBundleResponse.builder()
                    .contentId(contentId)
                    .title(content.getTitle())
                    .category(content.getCategory())
                    .videos(videosByContentId.getOrDefault(contentId, List.of()).stream()
                            .map(ContentVideoResponse::from)
                            .toList())
                    .albums(albums)
                    .tracks(tracks)
                    .build());
        }

        return SpotMediaResponse.builder()
                .spotId(spotId)
                .contents(bundles)
                .build();
    }

    @Transactional
    public ContentVideoResponse updateVideoHidden(Long videoId, boolean hidden) {
        ContentVideo video = workVideoRepository.findById(videoId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_VIDEO_NOT_FOUND));
        video.updateHidden(hidden);
        return ContentVideoResponse.from(video);
    }

    @Transactional
    public ContentTrackResponse updateTrackHidden(Long trackId, boolean hidden) {
        ContentTrack track = workTrackRepository.findById(trackId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_TRACK_NOT_FOUND));
        track.updateHidden(hidden);
        return ContentTrackResponse.from(track);
    }

    @Transactional
    public ContentAlbumResponse updateAlbumHidden(Long albumId, boolean hidden) {
        ContentAlbum album = contentAlbumRepository.findById(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_ALBUM_NOT_FOUND));
        album.updateHidden(hidden);
        return ContentAlbumResponse.from(album);
    }

    private List<ContentAlbumResponse> toAlbumResponses(List<ContentAlbum> albums) {
        if (albums.isEmpty()) {
            return List.of();
        }
        List<Long> albumIds = albums.stream().map(ContentAlbum::getContentAlbumId).toList();
        Map<Long, List<ContentTrackResponse>> tracksByAlbumId = workTrackRepository
                .findByContentAlbumIdInAndHiddenFalseOrderByIdAsc(albumIds).stream()
                .collect(Collectors.groupingBy(
                        ContentTrack::getContentAlbumId,
                        LinkedHashMap::new,
                        Collectors.mapping(ContentTrackResponse::from, Collectors.toList())
                ));
        return albums.stream()
                .map(album -> ContentAlbumResponse.from(
                        album,
                        tracksByAlbumId.getOrDefault(album.getContentAlbumId(), List.of())
                ))
                .toList();
    }
}
