package com.Wavey.WaveyService.domain.content.policy;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * 스포티파이 공식 OST 문. 플레이리스트는 여기로 들어오지 않는다.
 */
@Component
@RequiredArgsConstructor
public class SpotifyOstPolicy {

    private final MediaCollectProperties properties;

    public boolean shouldKeepAlbum(String albumName, String workTitle, String titleEn) {
        if (!StringUtils.hasText(albumName)) {
            return false;
        }
        if (!containsAny(albumName, properties.getSpotify().getOstMarkers())) {
            return false;
        }
        if (containsAny(albumName, properties.getSpotify().getAlbumDenyKeywords())) {
            return false;
        }
        return containsWorkTitle(albumName, workTitle, titleEn);
    }

    public boolean shouldKeepAlbumTrack(String trackName, Long durationMs) {
        if (!StringUtils.hasText(trackName)) {
            return false;
        }
        if (isTooLong(durationMs)) {
            return false;
        }
        return !containsAny(trackName, properties.getSpotify().getTrackDenyKeywords());
    }

    public boolean shouldKeepKpopTrack(String trackName, String albumName, Long durationMs) {
        if (!StringUtils.hasText(trackName)) {
            return false;
        }
        if (isTooLong(durationMs)) {
            return false;
        }
        if (containsAny(trackName, properties.getSpotify().getTrackDenyKeywords())) {
            return false;
        }
        return !containsAny(albumName, properties.getSpotify().getAlbumDenyKeywords());
    }

    private boolean isTooLong(Long durationMs) {
        return durationMs != null && durationMs >= properties.getSpotify().getMaxDurationMs();
    }

    private boolean containsAny(String text, Iterable<String> keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String haystack = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword)
                    && haystack.contains(keyword.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsWorkTitle(String albumName, String workTitle, String titleEn) {
        if (StringUtils.hasText(workTitle) && containsAny(albumName, List.of(workTitle))) {
            return true;
        }
        return StringUtils.hasText(titleEn) && containsAny(albumName, List.of(titleEn));
    }
}
