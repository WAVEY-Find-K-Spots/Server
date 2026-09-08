package com.Wavey.WaveyService.domain.content.external.client;

import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeDataClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${external-api.youtube.base-url}")
    private String baseUrl;

    @Value("${external-api.youtube.api-key}")
    private String apiKey;

    public YoutubeVideoDetails fetchVideo(String videoId) {
        List<YoutubeVideoDetails> videos = fetchVideos(List.of(videoId));
        if (videos.isEmpty()) {
            throw new CustomException(ErrorCode.YOUTUBE_VIDEO_NOT_FOUND);
        }
        return videos.get(0);
    }

    public List<String> searchVideoIds(String query, int maxResults) {
        validateApiKey();
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        try {
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(buildSearchUri(query, maxResults))
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode items = response == null ? null : response.path("items");
            if (items == null || !items.isArray()) {
                return List.of();
            }

            Set<String> videoIds = new LinkedHashSet<>();
            for (JsonNode item : items) {
                String videoId = text(item.path("id"), "videoId");
                if (StringUtils.hasText(videoId)) {
                    videoIds.add(videoId);
                }
            }
            return List.copyOf(videoIds);
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("YouTube search failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("YouTube search failed. query={}", query, e);
            throw new CustomException(ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        }
    }

    public List<YoutubeVideoDetails> fetchVideos(List<String> videoIds) {
        validateApiKey();
        if (videoIds == null || videoIds.isEmpty()) {
            return List.of();
        }

        try {
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(buildVideosUri(videoIds))
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode items = response == null ? null : response.path("items");
            if (items == null || !items.isArray() || items.isEmpty()) {
                return List.of();
            }

            List<YoutubeVideoDetails> details = new ArrayList<>();
            for (JsonNode item : items) {
                YoutubeVideoDetails parsed = parseVideo(item);
                if (parsed != null) {
                    details.add(parsed);
                }
            }
            return details;
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("YouTube API request failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("YouTube API request failed. videoIds={}", videoIds, e);
            throw new CustomException(ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        }
    }

    private YoutubeVideoDetails parseVideo(JsonNode item) {
        String videoId = text(item, "id");
        if (!StringUtils.hasText(videoId)) {
            return null;
        }
        JsonNode snippet = item.path("snippet");
        return new YoutubeVideoDetails(
                videoId,
                text(snippet, "title"),
                text(snippet, "description"),
                thumbnailUrl(snippet),
                text(snippet, "channelTitle"),
                parseDurationSec(text(item.path("contentDetails"), "duration"))
        );
    }

    private URI buildSearchUri(String query, int maxResults) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", Math.max(1, Math.min(maxResults, 10)))
                .queryParam("q", query)
                .queryParam("relevanceLanguage", "ko")
                .queryParam("safeSearch", "moderate")
                .queryParam("key", apiKey)
                .encode()
                .build()
                .toUri();
    }

    private URI buildVideosUri(List<String> videoIds) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/videos")
                .queryParam("part", "snippet,contentDetails,statistics")
                .queryParam("id", String.join(",", videoIds))
                .queryParam("key", apiKey)
                .build(true)
                .toUri();
    }

    private Integer parseDurationSec(String isoDuration) {
        if (!StringUtils.hasText(isoDuration)) {
            return null;
        }
        try {
            return Math.toIntExact(Duration.parse(isoDuration).toSeconds());
        } catch (RuntimeException e) {
            log.warn("YouTube duration parse failed. value={}", isoDuration);
            return null;
        }
    }

    private String thumbnailUrl(JsonNode snippet) {
        JsonNode thumbnails = snippet.path("thumbnails");
        for (String quality : new String[] {"high", "medium", "default"}) {
            String url = text(thumbnails.path(quality), "url");
            if (StringUtils.hasText(url)) {
                return url;
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(apiKey)) {
            throw new CustomException(ErrorCode.YOUTUBE_API_KEY_MISSING);
        }
    }
}
