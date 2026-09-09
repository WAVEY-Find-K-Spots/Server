package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.content.repository.*;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
import com.Wavey.WaveyService.domain.stamp.repository.UserStampRepository;
import com.Wavey.WaveyService.domain.user.repository.SavedSpotRepository;
import com.Wavey.WaveyService.global.common.UiSupport;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotDetailService {
    private final SpotDiscoveryService discovery;
    private final SavedSpotRepository saves;
    private final UserStampRepository stamps;
    private final RouteRepository routes;
    private final SpotContentRepository links;
    private final ContentRepository contents;

    public SpotResponse detail(Long id, Long userId, String language) {
        String lang = discovery.language(userId, language);
        var s = discovery.require(id);
        var r = SpotConverter.toResponse(s);
        r.setName(UiSupport.localized(s.getName(), s.getNameEn(), lang));
        r.setAddress(UiSupport.localized(s.getAddress(), s.getAddressEn(), lang));
        r.setDescription(UiSupport.localized(s.getDescription(), s.getDescriptionEn(), lang));
        r.setOpeningHours(UiSupport.localized(s.getOpeningHours(), s.getOpeningHoursEn(), lang));
        r.setClosedDays(UiSupport.localized(s.getClosedDays(), s.getClosedDaysEn(), lang));
        r.setTransportInfo(UiSupport.localized(s.getTransportInfo(), s.getTransportInfoEn(), lang));
        r.setCategoryCode(UiSupport.categoryCode(s.getCategory()));
        r.setCategoryLabel(UiSupport.categoryLabel(s.getCategory(), lang));
        r.setMapProvider("GOOGLE_MAPS");
        r.setGoogleMapsUrl(
                "https://www.google.com/maps/search/?api=1&query="
                        + s.getLatitude()
                        + "%2C"
                        + s.getLongitude());
        r.setReviewCount(s.getReviewCount());
        r.setSaved(saves.existsByUserIdAndSpotId(userId, id));
        r.setStampAcquired(
                stamps.findByUserId(userId).stream().anyMatch(x -> x.getSpotId().equals(id)));
        r.setRouteIds(
                routes.findByUserId(userId).stream()
                        .filter(
                                x ->
                                        x.getRouteSpots().stream()
                                                .anyMatch(a -> a.getSpotId().equals(id)))
                        .map(x -> x.getId())
                        .toList());
        var tags = new LinkedHashSet<String>();
        if (s.getTitle() != null && !s.getTitle().isBlank()) tags.add(s.getTitle());
        links.findBySpotIdOrderByIdAsc(id)
                .forEach(
                        link ->
                                contents.findById(link.getContentId())
                                        .ifPresent(
                                                c ->
                                                        tags.add(
                                                                UiSupport.localized(
                                                                        c.getTitle(),
                                                                        null,
                                                                        lang))));
        r.setTags(List.copyOf(tags));
        return r;
    }
}
