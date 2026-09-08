package com.Wavey.WaveyService.domain.content.external.client;

import com.Wavey.WaveyService.domain.content.external.dto.SpotifyAlbumTracks;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifySearchTrack;
import com.Wavey.WaveyService.domain.content.external.dto.SpotifyTrackDetails;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Spotify Web API 클라이언트.
 * Client Credentials로 앱 토큰을 받은 뒤 트랙 메타데이터를 조회한다.
 * 사용자 스포티파이 로그인은 쓰지 않는다. 플레이리스트는 검색하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpotifyApiClient {

    private static final String TOKEN_GRANT_TYPE = "grant_type=client_credentials";
    private static final long TOKEN_REFRESH_SKEW_SECONDS = 60L;

    private final RestClient.Builder restClientBuilder;

    @Value("${external-api.spotify.client-id:}")
    private String clientId;

    @Value("${external-api.spotify.client-secret:}")
    private String clientSecret;

    @Value("${external-api.spotify.token-url}")
    private String tokenUrl;

    @Value("${external-api.spotify.base-url}")
    private String baseUrl;

    private volatile CachedToken cachedToken;

    public SpotifyTrackDetails fetchTrack(String trackId) {
        validateCredentials();

        try {
            return withAccessToken(token -> requestTrack(trackId, token));
        } catch (RestClientResponseException e) {
            throw mapTrackException(trackId, e);
        } catch (RestClientException e) {
            log.warn("Spotify track request failed. trackId={}", trackId, e);
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        }
    }

    public List<SpotifySearchTrack> searchTracks(String query, int limit) {
        validateCredentials();
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        try {
            JsonNode response = withAccessToken(token -> restClientBuilder.build()
                    .get()
                    .uri(buildSearchUri(query, "track", limit))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(JsonNode.class));

            JsonNode items = response == null ? null : response.path("tracks").path("items");
            if (items == null || !items.isArray()) {
                return List.of();
            }

            List<SpotifySearchTrack> tracks = new ArrayList<>();
            for (JsonNode item : items) {
                if (item == null || item.isNull() || item.isMissingNode()) {
                    continue;
                }
                SpotifySearchTrack track = parseSearchTrack(item);
                if (track != null) {
                    tracks.add(track);
                }
            }
            return tracks;
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("Spotify search failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("Spotify search failed. query={}", query, e);
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        }
    }

    public List<SpotifyAlbumTracks> searchOstAlbums(String query, int limit) {
        validateCredentials();
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        try {
            JsonNode response = withAccessToken(token -> restClientBuilder.build()
                    .get()
                    .uri(buildSearchUri(query, "album", limit))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(JsonNode.class));

            JsonNode items = response == null ? null : response.path("albums").path("items");
            if (items == null || !items.isArray()) {
                return List.of();
            }

            List<SpotifyAlbumTracks> albums = new ArrayList<>();
            for (JsonNode item : items) {
                if (item == null || item.isNull() || item.isMissingNode()) {
                    continue;
                }
                String albumId = text(item, "id");
                if (StringUtils.hasText(albumId)) {
                    albums.add(new SpotifyAlbumTracks(
                            albumId,
                            text(item, "name"),
                            firstImageUrl(item.path("images")),
                            List.of()
                    ));
                }
            }
            return albums;
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("Spotify album search failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("Spotify album search failed. query={}", query, e);
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        }
    }

    public SpotifyAlbumTracks fetchAlbumTracks(String albumId) {
        validateCredentials();
        if (!StringUtils.hasText(albumId)) {
            return null;
        }

        try {
            JsonNode response = withAccessToken(token -> restClientBuilder.build()
                    .get()
                    .uri(buildAlbumUri(albumId))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(JsonNode.class));

            if (response == null || !StringUtils.hasText(text(response, "id"))) {
                return null;
            }

            String albumName = text(response, "name");
            String imageUrl = firstImageUrl(response.path("images"));
            List<SpotifySearchTrack> tracks = new ArrayList<>();
            JsonNode items = response.path("tracks").path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    SpotifySearchTrack track = parseAlbumTrack(item, albumId, albumName, imageUrl);
                    if (track != null) {
                        tracks.add(track);
                    }
                }
            }
            return new SpotifyAlbumTracks(albumId, albumName, imageUrl, tracks);
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("Spotify album request failed. albumId={}, status={}, body={}",
                    albumId, e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (RestClientException e) {
            log.warn("Spotify album request failed. albumId={}", albumId, e);
            return null;
        }
    }

    private SpotifyTrackDetails requestTrack(String trackId, String accessToken) {
        JsonNode response = restClientBuilder.build()
                .get()
                .uri(buildTrackUri(trackId))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(JsonNode.class);

        String title = text(response, "name");
        if (!StringUtils.hasText(title)) {
            throw new CustomException(ErrorCode.SPOTIFY_TRACK_NOT_FOUND);
        }

        return new SpotifyTrackDetails(
                trackId,
                title,
                joinArtistNames(response.path("artists")),
                text(response.path("album"), "name"),
                firstImageUrl(response.path("album").path("images"))
        );
    }

    private SpotifySearchTrack parseSearchTrack(JsonNode item) {
        String trackId = text(item, "id");
        String title = text(item, "name");
        if (!StringUtils.hasText(trackId) || !StringUtils.hasText(title)) {
            return null;
        }
        JsonNode album = item.path("album");
        return new SpotifySearchTrack(
                trackId,
                title,
                joinArtistNames(item.path("artists")),
                text(album, "id"),
                text(album, "name"),
                firstImageUrl(album.path("images")),
                longValue(item, "duration_ms"),
                text(item, "preview_url"),
                text(item.path("external_urls"), "spotify")
        );
    }

    private SpotifySearchTrack parseAlbumTrack(JsonNode item, String albumId, String albumName, String imageUrl) {
        String trackId = text(item, "id");
        String title = text(item, "name");
        if (!StringUtils.hasText(trackId) || !StringUtils.hasText(title)) {
            return null;
        }
        return new SpotifySearchTrack(
                trackId,
                title,
                joinArtistNames(item.path("artists")),
                albumId,
                albumName,
                imageUrl,
                longValue(item, "duration_ms"),
                text(item, "preview_url"),
                text(item.path("external_urls"), "spotify")
        );
    }

    private <T> T withAccessToken(Function<String, T> action) {
        boolean retried = false;
        while (true) {
            try {
                return action.apply(getAccessToken());
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value() && !retried) {
                    invalidateToken();
                    retried = true;
                    continue;
                }
                throw e;
            }
        }
    }

    private String getAccessToken() {
        CachedToken current = cachedToken;
        if (current != null && current.isValid()) {
            return current.accessToken();
        }

        synchronized (this) {
            current = cachedToken;
            if (current != null && current.isValid()) {
                return current.accessToken();
            }
            cachedToken = requestToken();
            return cachedToken.accessToken();
        }
    }

    private CachedToken requestToken() {
        try {
            JsonNode response = restClientBuilder.build()
                    .post()
                    .uri(URI.create(tokenUrl))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encodeCredentials())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(TOKEN_GRANT_TYPE)
                    .retrieve()
                    .body(JsonNode.class);

            String accessToken = text(response, "access_token");
            if (!StringUtils.hasText(accessToken)) {
                throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
            }

            long expiresIn = response == null ? 3600L : Math.max(response.path("expires_in").asLong(3600L), 1L);
            Instant expiresAt = Instant.now().plusSeconds(Math.max(expiresIn - TOKEN_REFRESH_SKEW_SECONDS, 1L));
            return new CachedToken(accessToken, expiresAt);
        } catch (CustomException e) {
            throw e;
        } catch (RestClientResponseException e) {
            log.warn("Spotify token request failed. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        } catch (RestClientException e) {
            log.warn("Spotify token request failed.", e);
            throw new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
        }
    }

    /**
     * Spotify 검색(/search)은 2026 기준 limit 최대 10.
     * 11 이상이면 400 Invalid limit. 앨범 트랙 조회(/albums/{id})는 이 제한이 아니다.
     * 쿼리스트링에 {limit} 템플릿을 넣으면 값이 안 들어가므로 URI를 직접 만든다.
     */
    private URI buildSearchUri(String query, String type, int limit) {
        int clamped = Math.max(1, Math.min(limit, 10));
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return URI.create(baseUrl + "/search?q=" + encodedQuery
                + "&type=" + type
                + "&limit=" + clamped
                + "&market=KR");
    }

    private URI buildAlbumUri(String albumId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/albums/{id}")
                .queryParam("market", "KR")
                .buildAndExpand(albumId)
                .toUri();
    }

    private URI buildTrackUri(String trackId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/tracks/{id}")
                .queryParam("market", "KR")
                .buildAndExpand(trackId)
                .toUri();
    }

    private CustomException mapTrackException(String trackId, RestClientResponseException e) {
        if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            return new CustomException(ErrorCode.SPOTIFY_TRACK_NOT_FOUND);
        }
        log.warn("Spotify track request failed. trackId={}, status={}, body={}",
                trackId, e.getStatusCode(), e.getResponseBodyAsString());
        return new CustomException(ErrorCode.SPOTIFY_API_REQUEST_FAILED);
    }

    private String joinArtistNames(JsonNode artists) {
        if (artists == null || !artists.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<>();
        for (JsonNode artist : artists) {
            String name = text(artist, "name");
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return names.isEmpty() ? null : String.join(", ", names);
    }

    private String firstImageUrl(JsonNode images) {
        if (images == null || !images.isArray() || images.isEmpty()) {
            return null;
        }
        return text(images.get(0), "url");
    }

    private Long longValue(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull() || !value.isNumber()) {
            return null;
        }
        return value.asLong();
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private String encodeCredentials() {
        return Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
    }

    private void invalidateToken() {
        cachedToken = null;
    }

    private void validateCredentials() {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new CustomException(ErrorCode.SPOTIFY_CREDENTIALS_MISSING);
        }
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        private boolean isValid() {
            return StringUtils.hasText(accessToken) && Instant.now().isBefore(expiresAt);
        }
    }
}
