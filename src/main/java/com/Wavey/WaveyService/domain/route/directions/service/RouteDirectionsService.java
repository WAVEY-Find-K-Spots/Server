package com.Wavey.WaveyService.domain.route.directions.service;

import com.Wavey.WaveyService.domain.route.directions.client.RouteLeg;
import com.Wavey.WaveyService.domain.route.directions.client.TmapDirectionsClient;
import com.Wavey.WaveyService.domain.route.dto.request.RouteDirectionsRequest;
import com.Wavey.WaveyService.domain.route.dto.response.GeoLineString;
import com.Wavey.WaveyService.domain.route.dto.response.RouteDirectionsResponse;
import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.entity.TransportMode;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.route.repository.RouteRepository;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteDirectionsService {

    private static final int MIN_SPOTS = 2;

    private final RouteRepository routeRepository;
    private final SpotRepository spotRepository;
    private final TmapDirectionsClient tmapDirectionsClient;

    private final Map<String, CachedDirections> cache = new ConcurrentHashMap<>();

    @Value("${tmap.directions-cache-ttl-seconds:300}")
    private long cacheTtlSeconds;

    public RouteDirectionsResponse getDirections(Long routeId, RouteDirectionsRequest request, Long userId) {
        TransportMode mode = request.getTransportMode();
        if (mode == null) {
            throw new CustomException(ErrorCode.DIRECTIONS_UNSUPPORTED_MODE);
        }

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTE_NOT_FOUND));
        validateAccess(route, userId);

        List<RouteSpot> orderedSpots = route.getRouteSpots().stream()
                .sorted(Comparator.comparing(RouteSpot::getSequenceOrder))
                .toList();

        if (orderedSpots.size() < MIN_SPOTS) {
            throw new CustomException(ErrorCode.DIRECTIONS_NOT_ENOUGH_SPOTS);
        }

        String cacheKey = cacheKey(routeId, mode, orderedSpots);
        CachedDirections cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired(cacheTtlSeconds)) {
            return cached.response();
        }

        Map<Long, Spot> spotMap = spotRepository.findAllById(
                        orderedSpots.stream().map(RouteSpot::getSpotId).toList()).stream()
                .collect(Collectors.toMap(Spot::getId, Function.identity()));

        RouteDirectionsResponse response = calculate(routeId, mode, orderedSpots, spotMap);
        cache.put(cacheKey, new CachedDirections(response, LocalDateTime.now()));
        return response;
    }

    private RouteDirectionsResponse calculate(
            Long routeId,
            TransportMode mode,
            List<RouteSpot> orderedSpots,
            Map<Long, Spot> spotMap
    ) {
        List<RouteDirectionsResponse.Segment> segments = new ArrayList<>();
        List<List<Double>> fullPath = new ArrayList<>();
        long totalDistance = 0L;
        long totalDuration = 0L;

        for (int i = 0; i < orderedSpots.size() - 1; i++) {
            RouteSpot fromRouteSpot = orderedSpots.get(i);
            RouteSpot toRouteSpot = orderedSpots.get(i + 1);
            Spot from = requireSpot(spotMap, fromRouteSpot.getSpotId());
            Spot to = requireSpot(spotMap, toRouteSpot.getSpotId());

            RouteLeg leg = tmapDirectionsClient.route(
                    mode,
                    from.getLongitude().doubleValue(), from.getLatitude().doubleValue(),
                    to.getLongitude().doubleValue(), to.getLatitude().doubleValue()
            );

            totalDistance += leg.distanceMeters();
            totalDuration += leg.durationSeconds();

            List<List<Double>> segmentPath = toCoordinateList(leg);
            appendPath(fullPath, segmentPath);

            segments.add(RouteDirectionsResponse.Segment.builder()
                    .fromRouteSpotId(fromRouteSpot.getId())
                    .toRouteSpotId(toRouteSpot.getId())
                    .fromSpotId(fromRouteSpot.getSpotId())
                    .toSpotId(toRouteSpot.getSpotId())
                    .sequenceOrder(i + 1)
                    .distanceMeters(leg.distanceMeters())
                    .durationSeconds(leg.durationSeconds())
                    .durationText(durationText(mode, leg.durationSeconds()))
                    .geometry(GeoLineString.of(segmentPath))
                    .build());
        }

        RouteDirectionsResponse.Total total = RouteDirectionsResponse.Total.builder()
                .distanceMeters(totalDistance)
                .durationSeconds(totalDuration)
                .distanceText(distanceText(totalDistance))
                .durationText(totalDurationText(totalDuration))
                .build();

        return RouteDirectionsResponse.builder()
                .routeId(routeId)
                .transportMode(mode)
                .total(total)
                .segments(segments)
                .geometry(GeoLineString.of(fullPath))
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private Spot requireSpot(Map<Long, Spot> spotMap, Long spotId) {
        Spot spot = spotMap.get(spotId);
        if (spot == null || spot.getLatitude() == null || spot.getLongitude() == null) {
            throw new CustomException(ErrorCode.SPOT_NOT_FOUND);
        }
        return spot;
    }

    private void validateAccess(Route route, Long userId) {
        if (route.getVisibility() == Visibility.PRIVATE && !route.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ROUTE_FORBIDDEN);
        }
    }

    private List<List<Double>> toCoordinateList(RouteLeg leg) {
        List<List<Double>> coordinates = new ArrayList<>();
        for (double[] coord : leg.path()) {
            coordinates.add(List.of(coord[0], coord[1]));
        }
        return coordinates;
    }

    private void appendPath(List<List<Double>> target, List<List<Double>> source) {
        for (List<Double> coord : source) {
            if (!target.isEmpty() && target.get(target.size() - 1).equals(coord)) {
                continue;
            }
            target.add(coord);
        }
    }

    private String cacheKey(Long routeId, TransportMode mode, List<RouteSpot> orderedSpots) {
        String composition = orderedSpots.stream()
                .map(spot -> spot.getSpotId() + "@" + spot.getSequenceOrder())
                .collect(Collectors.joining(","));
        return routeId + ":" + mode + ":" + composition.hashCode();
    }

    private String distanceText(long meters) {
        if (meters < 1000) {
            return meters + "m";
        }
        return String.format("%.1fkm", meters / 1000.0);
    }

    private String totalDurationText(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        if (hours > 0) {
            return "약 " + hours + "시간 " + minutes + "분";
        }
        return "약 " + minutes + "분";
    }

    private String durationText(TransportMode mode, long seconds) {
        long minutes = Math.max(1, Math.round(seconds / 60.0));
        return mode.getLabel() + " " + minutes + "분";
    }

    private record CachedDirections(RouteDirectionsResponse response, LocalDateTime cachedAt) {

        boolean isExpired(long ttlSeconds) {
            return cachedAt.plusSeconds(ttlSeconds).isBefore(LocalDateTime.now());
        }
    }
}
