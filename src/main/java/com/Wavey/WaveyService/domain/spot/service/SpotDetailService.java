package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.converter.SpotConverter;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotResponse;
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
    private final RouteRepository routes;

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
        r.setSaved(false);
        r.setStampAcquired(false);
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
        r.setTags(List.copyOf(tags));
        return r;
    }
}
