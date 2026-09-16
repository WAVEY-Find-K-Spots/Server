package com.Wavey.WaveyService.domain.content.policy;

import com.Wavey.WaveyService.domain.content.config.MediaCollectProperties;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
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

    public boolean shouldKeep(
            YoutubeVideoDetails video,
            ContentCategory category,
            String workTitle,
            String titleEn,
            Set<String> hiddenVideoIds
    ) {
        if (video == null || !StringUtils.hasText(video.videoId()) || !StringUtils.hasText(video.title())) {
            return false;
        }
        if (hiddenVideoIds.contains(video.videoId())) {
            return false;
        }
        if (category == ContentCategory.HERITAGE) {
            return shouldKeepHeritage(video, workTitle, titleEn);
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
        return containsContentTitle(video.title(), workTitle, titleEn);
    }

    private boolean shouldKeepHeritage(YoutubeVideoDetails video, String workTitle, String titleEn) {
        Integer durationSec = video.durationSec();
        if (durationSec == null
                || durationSec < properties.getHeritage().getMinDurationSec()
                || durationSec >= properties.getHeritage().getMaxDurationSec()) {
            return false;
        }
        if (containsAny(video.title(), properties.getHeritage().getDenyKeywords())
                || containsAny(video.description(), properties.getHeritage().getDenyKeywords())) {
            return false;
        }
        boolean titleMatched = containsHeritageContentReference(video.title(), workTitle, titleEn);
        boolean descriptionMatched = containsHeritageContentReference(video.description(), workTitle, titleEn);
        if (!titleMatched && !descriptionMatched) {
            return false;
        }
        return containsAny(video.title(), properties.getHeritage().getAllowKeywords())
                || containsAny(video.description(), properties.getHeritage().getAllowKeywords())
                || containsAny(video.channelTitle(), properties.getHeritage().getTrustedChannelKeywords());
    }

    private boolean containsContentTitle(String title, String workTitle, String titleEn) {
        if (containsNormalized(title, workTitle)) {
            return true;
        }
        return StringUtils.hasText(titleEn) && containsNormalized(title, titleEn);
    }

    /**
     * HERITAGE는 영상 제목이 콘텐츠 전체명과 다를 수 있다.
     * 예: 콘텐츠 "경복궁 근정전", 영상 "경복궁 소개영상"
     */
    private boolean containsHeritageContentReference(String text, String workTitle, String titleEn) {
        if (containsContentTitle(text, workTitle, titleEn)) {
            return true;
        }
        return containsAnyNameToken(text, workTitle, 2)
                || containsAnyNameToken(text, titleEn, 3);
    }

    private boolean containsAnyNameToken(String text, String phrase, int minTokenLength) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(phrase)) {
            return false;
        }
        for (String token : phrase.trim().split("\\s+")) {
            if (token.length() >= minTokenLength && containsNormalized(text, token)) {
                return true;
            }
        }
        return false;
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
