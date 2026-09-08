package com.Wavey.WaveyService.domain.content.policy;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.external.dto.YoutubeVideoDetails;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 유튜브 예고편 문. 검색 클라이언트가 가져온 후보를 KEEP/DROP만 판단한다.
 */
@Component
@RequiredArgsConstructor
public class YoutubePromoPolicy {

    private final MediaCollectProperties properties;

    public boolean shouldKeep(YoutubeVideoDetails video, String workTitle, String titleEn, Set<String> hiddenVideoIds) {
        if (video == null || !StringUtils.hasText(video.videoId()) || !StringUtils.hasText(video.title())) {
            return false;
        }
        if (hiddenVideoIds.contains(video.videoId())) {
            return false;
        }
        if (video.durationSec() == null || video.durationSec() >= properties.getYoutube().getMaxDurationSec()) {
            return false;
        }
        if (!containsAny(video.title(), properties.getYoutube().getAllowKeywords())) {
            return false;
        }
        if (containsAny(video.title(), properties.getYoutube().getDenyKeywords())) {
            return false;
        }
        return containsWorkTitle(video.title(), workTitle, titleEn);
    }

    private boolean containsWorkTitle(String title, String workTitle, String titleEn) {
        if (containsNormalized(title, workTitle)) {
            return true;
        }
        return StringUtils.hasText(titleEn) && containsNormalized(title, titleEn);
    }

    private boolean containsAny(String text, Iterable<String> keywords) {
        for (String keyword : keywords) {
            if (containsNormalized(text, keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNormalized(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }
}
