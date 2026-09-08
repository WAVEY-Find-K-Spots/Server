package com.Wavey.WaveyService.domain.content.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private static final Pattern YOUTUBE_SHORTS_PATTERN = Pattern.compile("/shorts/([A-Za-z0-9_-]{11})");
    private static final Pattern YOUTUBE_EMBED_PATTERN = Pattern.compile("/embed/([A-Za-z0-9_-]{11})");
    private static final Pattern SPOTIFY_TRACK_PATTERN = Pattern.compile("/track/([A-Za-z0-9]+)");
    private static final String YOUTUBE_THUMBNAIL_TEMPLATE = "https://i.ytimg.com/vi/%s/hqdefault.jpg";

    private final ContentRepository contentRepository;
    private final YoutubeDataClient youtubeDataClient;
    private final SpotifyApiClient spotifyApiClient;

    @Transactional
    public ContentResponse createYoutube(ContentRequest request) {
        String videoId = extractYoutubeVideoId(request.getUrl());
        validateDuplicate(ContentPlatform.YOUTUBE, videoId);
        YoutubeVideoDetails details = youtubeDataClient.fetchVideo(videoId);

        Content content = Content.builder()
                .platform(ContentPlatform.YOUTUBE)
                .title(resolveYoutubeTitle(request.getTitle(), details))
                .description(resolveYoutubeDescription(request.getDescription(), details))
                .externalId(videoId)
                .thumbnailUrl(resolveYoutubeThumbnailUrl(videoId, details))
                .build();

        return toResponse(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> searchYoutube(String keyword) {
        return contentRepository.findByPlatformAndTitleContainingIgnoreCase(ContentPlatform.YOUTUBE, normalizeKeyword(keyword))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContentResponse updateYoutube(Long contentId, ContentRequest request) {
        Content content = getContent(contentId, ContentPlatform.YOUTUBE);
        String videoId = extractYoutubeVideoId(request.getUrl());
        validateDuplicateOnUpdate(contentId, ContentPlatform.YOUTUBE, videoId);
        YoutubeVideoDetails details = youtubeDataClient.fetchVideo(videoId);
        content.update(
                resolveYoutubeTitle(request.getTitle(), details),
                resolveYoutubeDescription(request.getDescription(), details),
                videoId,
                resolveYoutubeThumbnailUrl(videoId, details)
        );
        return toResponse(content);
    }

    @Transactional
    public void deleteYoutube(Long contentId) {
        contentRepository.delete(getContent(contentId, ContentPlatform.YOUTUBE));
    }

    @Transactional
    public ContentResponse createSpotify(ContentRequest request) {
        String songId = extractSpotifySongId(request.getUrl());
        validateDuplicate(ContentPlatform.SPOTIFY, songId);
        SpotifyTrackDetails details = spotifyApiClient.fetchTrack(songId);

        Content content = Content.builder()
                .platform(ContentPlatform.SPOTIFY)
                .title(resolveSpotifyTitle(request.getTitle(), details))
                .description(resolveSpotifyDescription(request.getDescription(), details))
                .externalId(songId)
                .thumbnailUrl(resolveSpotifyThumbnailUrl(details))
                .build();

        return toResponse(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> searchSpotify(String keyword) {
        return contentRepository.findByPlatformAndTitleContainingIgnoreCase(ContentPlatform.SPOTIFY, normalizeKeyword(keyword))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContentResponse updateSpotify(Long contentId, ContentRequest request) {
        Content content = getContent(contentId, ContentPlatform.SPOTIFY);
        String songId = extractSpotifySongId(request.getUrl());
        validateDuplicateOnUpdate(contentId, ContentPlatform.SPOTIFY, songId);
        SpotifyTrackDetails details = spotifyApiClient.fetchTrack(songId);
        content.update(
                resolveSpotifyTitle(request.getTitle(), details),
                resolveSpotifyDescription(request.getDescription(), details),
                songId,
                resolveSpotifyThumbnailUrl(details)
        );
        return toResponse(content);
    }

    @Transactional
    public void deleteSpotify(Long contentId) {
        contentRepository.delete(getContent(contentId, ContentPlatform.SPOTIFY));
    }

    private Content getContent(Long contentId, ContentPlatform platform) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTENT_NOT_FOUND));
        if (content.getPlatform() != platform) {
            throw new CustomException(ErrorCode.CONTENT_NOT_FOUND);
        }
        return content;
    }

    private void validateDuplicate(ContentPlatform platform, String externalId) {
        if (contentRepository.findByPlatformAndExternalId(platform, externalId).isPresent()) {
            throw new CustomException(ErrorCode.CONTENT_ALREADY_EXISTS);
        }
    }

    private void validateDuplicateOnUpdate(Long contentId, ContentPlatform platform, String externalId) {
        contentRepository.findByPlatformAndExternalId(platform, externalId)
                .filter(existing -> !existing.getContentId().equals(contentId))
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.CONTENT_ALREADY_EXISTS);
                });
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private String resolveYoutubeTitle(String requestedTitle, YoutubeVideoDetails details) {
        if (StringUtils.hasText(requestedTitle)) {
            return requestedTitle.trim();
        }
        if (details != null && StringUtils.hasText(details.title())) {
            return details.title().trim();
        }
        throw new CustomException(ErrorCode.YOUTUBE_VIDEO_NOT_FOUND);
    }

    private String resolveYoutubeDescription(String requestedDescription, YoutubeVideoDetails details) {
        if (StringUtils.hasText(requestedDescription)) {
            return requestedDescription.trim();
        }
        if (details != null && StringUtils.hasText(details.description())) {
            return details.description().trim();
        }
        return null;
    }

    private String resolveYoutubeThumbnailUrl(String videoId, YoutubeVideoDetails details) {
        if (details != null && StringUtils.hasText(details.thumbnailUrl())) {
            return details.thumbnailUrl().trim();
        }
        return YOUTUBE_THUMBNAIL_TEMPLATE.formatted(videoId);
    }

    private String resolveSpotifyTitle(String requestedTitle, SpotifyTrackDetails details) {
        if (StringUtils.hasText(requestedTitle)) {
            return requestedTitle.trim();
        }
        if (details != null && StringUtils.hasText(details.title())) {
            return details.title().trim();
        }
        throw new CustomException(ErrorCode.SPOTIFY_TRACK_NOT_FOUND);
    }

    private String resolveSpotifyDescription(String requestedDescription, SpotifyTrackDetails details) {
        if (StringUtils.hasText(requestedDescription)) {
            return requestedDescription.trim();
        }
        if (details == null) {
            return null;
        }
        if (StringUtils.hasText(details.artistName()) && StringUtils.hasText(details.albumName())) {
            return details.artistName().trim() + " · " + details.albumName().trim();
        }
        if (StringUtils.hasText(details.artistName())) {
            return details.artistName().trim();
        }
        return null;
    }

    private String resolveSpotifyThumbnailUrl(SpotifyTrackDetails details) {
        if (details != null && StringUtils.hasText(details.thumbnailUrl())) {
            return details.thumbnailUrl().trim();
        }
        throw new CustomException(ErrorCode.CONTENT_THUMBNAIL_RESOLVE_FAILED);
    }

    private String extractYoutubeVideoId(String url) {
        String trimmedUrl = requireUrl(url);
        try {
            URI uri = new URI(trimmedUrl);
            String host = uri.getHost();
            if (host == null || (!host.contains("youtube.com") && !host.contains("youtu.be"))) {
                throw new CustomException(ErrorCode.INVALID_YOUTUBE_URL);
            }

            Map<String, String> queryParams = splitQuery(uri.getQuery());
            if (queryParams.containsKey("v")) {
                return validateYoutubeId(queryParams.get("v"));
            }

            Matcher shortsMatcher = YOUTUBE_SHORTS_PATTERN.matcher(uri.getPath());
            if (shortsMatcher.find()) {
                return shortsMatcher.group(1);
            }

            Matcher embedMatcher = YOUTUBE_EMBED_PATTERN.matcher(uri.getPath());
            if (embedMatcher.find()) {
                return embedMatcher.group(1);
            }

            if (host.contains("youtu.be")) {
                String path = uri.getPath();
                if (StringUtils.hasText(path) && path.length() > 1) {
                    return validateYoutubeId(path.substring(1));
                }
            }
        } catch (URISyntaxException e) {
            throw new CustomException(ErrorCode.INVALID_YOUTUBE_URL);
        }

        throw new CustomException(ErrorCode.INVALID_YOUTUBE_URL);
    }

    private String extractSpotifySongId(String url) {
        String trimmedUrl = requireUrl(url);
        try {
            URI uri = new URI(trimmedUrl);
            String host = uri.getHost();
            if (host == null || !host.contains("spotify")) {
                throw new CustomException(ErrorCode.INVALID_SPOTIFY_URL);
            }

            Matcher matcher = SPOTIFY_TRACK_PATTERN.matcher(uri.getPath());
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (URISyntaxException e) {
            throw new CustomException(ErrorCode.INVALID_SPOTIFY_URL);
        }

        throw new CustomException(ErrorCode.INVALID_SPOTIFY_URL);
    }

    private String requireUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new CustomException(ErrorCode.CONTENT_URL_REQUIRED);
        }
        return url.trim();
    }

    private String validateYoutubeId(String videoId) {
        if (StringUtils.hasText(videoId) && videoId.trim().matches("[A-Za-z0-9_-]{11}")) {
            return videoId.trim();
        }
        throw new CustomException(ErrorCode.INVALID_YOUTUBE_URL);
    }

    private Map<String, String> splitQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return Map.of();
        }

        return Pattern.compile("&")
                .splitAsStream(query)
                .map(pair -> pair.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1], (a, b) -> b));
    }

    private ContentResponse toResponse(Content content) {
        LocalDateTime createdAt = content.getCreatedAt();
        LocalDateTime updatedAt = content.getUpdatedAt();

        return ContentResponse.builder()
                .contentId(content.getContentId())
                .platform(content.getPlatform())
                .title(content.getTitle())
                .description(content.getDescription())
                .externalId(content.getExternalId())
                .thumbnailUrl(content.getThumbnailUrl())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
