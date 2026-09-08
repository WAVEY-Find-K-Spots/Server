package com.Wavey.WaveyService.domain.route.service;

import com.Wavey.WaveyService.domain.route.entity.*;
import com.Wavey.WaveyService.domain.route.repository.*;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.service.SpotDiscoveryService;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutePlanningService {
    private final RouteRepository routes;
    private final RouteSpotRepository routeSpots;
    private final RouteLegRepository legs;
    private final RouteNavigationRepository navigation;
    private final SpotDiscoveryService discovery;

    public record Leg(
            Long fromSpotId,
            Long toSpotId,
            Integer distanceMeters,
            Integer durationSeconds,
            String encodedPolyline,
            String instruction,
            boolean available) {}

    public record Plan(
            String mapProvider,
            RouteLeg.TravelMode mode,
            int spotCount,
            Long distanceMeters,
            Long durationSeconds,
            boolean routeAvailable,
            List<SpotListResponse> spots,
            List<Leg> legs) {}

    public record Progress(
            Long routeId,
            Plan plan,
            int currentIndex,
            int completedSpotCount,
            boolean completed,
            Long currentSpotId,
            Long nextSpotId,
            Long version) {}

    public Route accessible(Long routeId, Long userId, boolean ownerOnly) {
        Route r =
                routes.findById(routeId)
                        .orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));
        if (!r.getUserId().equals(userId) && (ownerOnly || r.getVisibility() == Visibility.PRIVATE))
            throw new CustomException(ErrorCode.ROUTE_FORBIDDEN);
        return r;
    }

    public Plan plan(Long routeId, Long userId, RouteLeg.TravelMode mode, String language) {
        var r = accessible(routeId, userId, false);
        return plan(r, userId, mode == null ? r.getTravelMode() : mode, language);
    }

    public Plan plan(Route r, Long userId, RouteLeg.TravelMode mode, String language) {
        String lang = discovery.language(userId, language);
        var ordered = routeSpots.findByRouteIdOrderBySequenceOrderAsc(r.getId());
        List<SpotListResponse> cards =
                ordered.stream()
                        .map(
                                s ->
                                        discovery.card(
                                                discovery.require(s.getSpotId()),
                                                lang,
                                                false,
                                                null,
                                                null))
                        .toList();
        List<Leg> result = new ArrayList<>();
        long distance = 0, duration = 0;
        boolean available = true;
        for (int i = 1; i < cards.size(); i++) {
            Long from = cards.get(i - 1).getSpotId(), to = cards.get(i).getSpotId();
            var data = legs.findByFromSpotIdAndToSpotIdAndMode(from, to, mode);
            if (data.isEmpty()) {
                available = false;
                result.add(new Leg(from, to, null, null, null, null, false));
            } else {
                var l = data.get();
                distance += l.getDistanceMeters();
                duration += l.getDurationSeconds();
                result.add(
                        new Leg(
                                from,
                                to,
                                l.getDistanceMeters(),
                                l.getDurationSeconds(),
                                l.getEncodedPolyline(),
                                UiSupport.localized(l.getInstruction(), l.getInstructionEn(), lang),
                                true));
            }
        }
        // Google Routes API calls are intentionally disabled in DB-only mode.
        // googleRoutesClient.computeRoutes(...);
        return new Plan(
                "GOOGLE_MAPS",
                mode,
                cards.size(),
                available ? distance : null,
                available ? duration : null,
                available,
                cards,
                result);
    }

    @Transactional
    public Plan setMode(Long id, Long userId, RouteLeg.TravelMode mode, String language) {
        var r = accessible(id, userId, true);
        r.changeTravelMode(mode);
        return plan(r, userId, mode, language);
    }

    private String snapshot(Plan p) {
        return p.spots().stream()
                .map(s -> s.getSpotId().toString())
                .collect(Collectors.joining(","));
    }

    @Transactional
    public Progress start(Long id, Long userId, RouteLeg.TravelMode mode, String language) {
        var r = accessible(id, userId, true);
        routes.findLockedById(id).orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));
        Plan p = plan(r, userId, mode == null ? r.getTravelMode() : mode, language);
        if (p.spots().isEmpty()) throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        var n =
                navigation
                        .findByRouteId(id)
                        .orElseGet(() -> RouteNavigation.builder().routeId(id).build());
        n.setMode(p.mode());
        n.setSpotIds(snapshot(p));
        n.setCurrentIndex(0);
        n.setCompleted(false);
        return progress(navigation.saveAndFlush(n), p);
    }

    public Progress current(Long id, Long userId, String language) {
        accessible(id, userId, true);
        var n =
                navigation
                        .findByRouteId(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        var p = plan(id, userId, n.getMode(), language);
        validateSnapshot(n, p);
        return progress(n, p);
    }

    @Transactional
    public Progress step(
            Long id,
            Long userId,
            int expectedIndex,
            Long expectedVersion,
            boolean next,
            String language) {
        accessible(id, userId, true);
        var n =
                navigation
                        .findByRouteId(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        var p = plan(id, userId, n.getMode(), language);
        validateSnapshot(n, p);
        if (n.getCurrentIndex() != expectedIndex
                || !Objects.equals(n.getVersion(), expectedVersion))
            throw new CustomException(ErrorCode.NAVIGATION_STALE);
        if (next) {
            if (n.getCurrentIndex() + 1 >= p.spotCount()) n.setCompleted(true);
            else n.setCurrentIndex(n.getCurrentIndex() + 1);
        } else {
            if (n.isCompleted()) n.setCompleted(false);
            else n.setCurrentIndex(Math.max(0, n.getCurrentIndex() - 1));
        }
        navigation.flush();
        return progress(n, p);
    }

    private void validateSnapshot(RouteNavigation n, Plan p) {
        if (!n.getSpotIds().equals(snapshot(p)))
            throw new CustomException(ErrorCode.NAVIGATION_STALE);
    }

    private Progress progress(RouteNavigation n, Plan p) {
        int i = n.getCurrentIndex();
        return new Progress(
                n.getRouteId(),
                p,
                i,
                n.isCompleted() ? p.spotCount() : i,
                n.isCompleted(),
                p.spots().get(i).getSpotId(),
                i + 1 < p.spotCount() ? p.spots().get(i + 1).getSpotId() : null,
                n.getVersion());
    }
}
