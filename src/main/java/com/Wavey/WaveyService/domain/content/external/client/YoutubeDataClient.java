package com.Wavey.WaveyService.domain.content.external.client;

import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
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
        validateApiKey();

        try {
            JsonNode response = restClientBuilder.build()
                    .get()
                    .uri(buildVideosUri(videoId))
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode item = firstItem(response);
            JsonNode snippet = item.path("snippet");

            return new YoutubeVideoDetails(
                    videoId,
                    text(snippet, "title"),
                    text(snippet, "description"),
                    thumbnailUrl(snippet),
                    text(snippet, "channelTitle")
            );
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("YouTube API request failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("YouTube API request failed. videoId={}", videoId, e);
            throw new CustomException(ErrorCode.YOUTUBE_API_REQUEST_FAILED);
        }
    }

    private URI buildVideosUri(String videoId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/videos")
                .queryParam("part", "snippet,contentDetails,statistics")
                .queryParam("id", videoId)
                .queryParam("key", apiKey)
                .build(true)
                .toUri();
    }

    private JsonNode firstItem(JsonNode response) {
        JsonNode items = response == null ? null : response.path("items");
        if (items == null || !items.isArray() || items.isEmpty()) {
            throw new CustomException(ErrorCode.YOUTUBE_VIDEO_NOT_FOUND);
        }
        return items.get(0);
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
