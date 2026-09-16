package com.Wavey.WaveyService.domain.spot.sync.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Wikimedia Commons API로 장소명을 검색해 재배포 가능한 라이선스의 대표 이미지를 찾는다.
 * 인증키가 필요 없는 공개 API이며, 캐싱/저장이 제한된 Google Places와 달리 CC 계열 라이선스는
 * 저작자 표시를 조건으로 재게시(우리 서버 재호스팅 포함)를 허용한다.
 */
@Slf4j
@Component
public class SpotWikimediaImageClient {

    private static final String BASE_URL = "https://commons.wikimedia.org/w/api.php";

    /** 붙여쓴 복합 장소명(예: "임진각평화누리")이 전체로는 안 잡힐 때, 끝에서부터 줄여가며 재시도할 최소 길이/횟수. */
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_TRIM_ATTEMPTS = 4;

    /** 재배포를 허용하는 라이선스만 채택한다. */
    private static final Set<String> ALLOWED_LICENSE_PREFIXES = Set.of(
            "cc0", "public domain", "cc by", "cc-by"
    );

    private final RestClient restClient;

    public SpotWikimediaImageClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .defaultHeader(HttpHeaders.USER_AGENT, "WaveyService/1.0 (K-content spot photo enrichment)")
                .build();
    }

    public record WikimediaImage(String imageUrl, String attribution) {}

    public Optional<WikimediaImage> findImage(String nameKo) {
        if (!StringUtils.hasText(nameKo)) {
            return Optional.empty();
        }

        String query = nameKo.trim();
        int attempts = 0;
        while (query.length() >= MIN_QUERY_LENGTH && attempts <= MAX_TRIM_ATTEMPTS) {
            Optional<WikimediaImage> result = search(query);
            if (result.isPresent()) {
                return result;
            }
            query = query.substring(0, query.length() - 1);
            attempts++;
        }
        return Optional.empty();
    }

    private Optional<WikimediaImage> search(String query) {
        try {
            String uri = UriComponentsBuilder.fromUriString(BASE_URL)
                    .queryParam("action", "query")
                    .queryParam("generator", "search")
                    .queryParam("gsrsearch", query)
                    .queryParam("gsrnamespace", 6)
                    .queryParam("gsrlimit", 5)
                    .queryParam("prop", "imageinfo")
                    .queryParam("iiprop", "url|extmetadata")
                    .queryParam("format", "json")
                    .build()
                    .toUriString();

            JsonNode response = restClient.get().uri(uri).retrieve().body(JsonNode.class);
            return extractFirstUsableImage(response);
        } catch (RestClientException e) {
            log.warn("Wikimedia Commons search failed for '{}'.", query, e);
            return Optional.empty();
        }
    }

    private Optional<WikimediaImage> extractFirstUsableImage(JsonNode response) {
        JsonNode pages = response == null
                ? null
                : response.path("query").path("pages");
        if (pages == null || !pages.isObject()) {
            return Optional.empty();
        }

        Iterator<JsonNode> it = pages.elements();
        while (it.hasNext()) {
            JsonNode page = it.next();
            JsonNode info = page.path("imageinfo");
            if (!info.isArray() || info.isEmpty()) {
                continue;
            }
            JsonNode first = info.get(0);
            JsonNode extmetadata = first.path("extmetadata");
            String license = extmetadata.path("LicenseShortName").path("value").asText("");
            if (!isReusableLicense(license)) {
                continue;
            }
            String url = first.path("url").asText(null);
            if (!StringUtils.hasText(url)) {
                continue;
            }
            String artist = stripHtml(extmetadata.path("Artist").path("value").asText(""));
            String attribution = buildAttribution(artist, license);
            return Optional.of(new WikimediaImage(url, attribution));
        }
        return Optional.empty();
    }

    private boolean isReusableLicense(String license) {
        if (!StringUtils.hasText(license)) {
            return false;
        }
        String normalized = license.toLowerCase(Locale.ROOT);
        return ALLOWED_LICENSE_PREFIXES.stream().anyMatch(normalized::contains);
    }

    private String buildAttribution(String artist, String license) {
        String who = StringUtils.hasText(artist) ? artist : "Wikimedia Commons contributor";
        String lic = StringUtils.hasText(license) ? license : "CC BY-SA";
        return "%s (%s, via Wikimedia Commons)".formatted(who, lic);
    }

    private String stripHtml(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("<[^>]*>", "").trim();
    }
}
