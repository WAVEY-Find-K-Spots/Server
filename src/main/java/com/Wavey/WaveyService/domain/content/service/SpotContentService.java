package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.entity.SpotContent;
import com.Wavey.WaveyService.domain.content.repository.*;
import com.Wavey.WaveyService.domain.spot.service.SpotDiscoveryService;
import com.Wavey.WaveyService.global.common.UiSupport;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotContentService {
    private final SpotContentRepository links;
    private final ContentRepository contents;
    private final SpotDiscoveryService discovery;

    public record Item(
            Long contentId,
            SpotContent.Kind kind,
            String title,
            String artist,
            String episodes,
            String description,
            String sceneDescription,
            String thumbnailUrl,
            String playbackUrl,
            String sceneUrl,
            Integer durationSeconds) {}

    public record Result(
            List<Item> dramas,
            List<Item> movies,
            List<Item> music,
            List<Item> videos,
            String playlistUrl) {}

    public Result get(Long spotId, Long userId, String language) {
        var spot = discovery.require(spotId);
        String lang = discovery.language(userId, language);
        List<Item> all = new ArrayList<>();
        for (var link : links.findBySpotIdOrderByDisplayOrderAscIdAsc(spotId))
            contents.findById(link.getContentId())
                    .ifPresent(
                            c ->
                                    all.add(
                                            new Item(
                                                    c.getId(),
                                                    link.getKind(),
                                                    UiSupport.localized(
                                                            c.getTitle(), link.getTitleEn(), lang),
                                                    link.getArtist(),
                                                    link.getEpisodes(),
                                                    UiSupport.localized(
                                                            c.getDescription(),
                                                            link.getDescriptionEn(),
                                                            lang),
                                                    UiSupport.localized(
                                                            link.getSceneDescription(),
                                                            link.getSceneDescriptionEn(),
                                                            lang),
                                                    c.getThumbnailUrl(),
                                                    link.getPlaybackUrl(),
                                                    link.getSceneUrl(),
                                                    link.getDurationSeconds())));
        return new Result(
                group(all, SpotContent.Kind.DRAMA),
                group(all, SpotContent.Kind.MOVIE),
                group(all, SpotContent.Kind.MUSIC),
                group(all, SpotContent.Kind.VIDEO),
                spot.getPlaylistUrl());
    }

    private List<Item> group(List<Item> items, SpotContent.Kind kind) {
        return items.stream().filter(i -> i.kind() == kind).toList();
    }
}
