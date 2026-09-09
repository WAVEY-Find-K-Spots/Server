package com.Wavey.WaveyService.domain.spot.service;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotListResponse;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.*;

import jakarta.persistence.criteria.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotDiscoveryService {
    private final SpotRepository spots;

    public String language(Long userId, String requested) {
        return UiSupport.language(requested);
    }

    public Spot require(Long id) {
        return spots.findById(id).orElseThrow(() -> new CustomException(ErrorCode.SPOT_NOT_FOUND));
    }

    public Page<SpotListResponse> search(SpotSearchRequest f, Long userId) {
        String lang = language(userId, f.getLanguage());
        boolean geo =
                f.getLatitude() != null
                        || f.getLongitude() != null
                        || f.getRadiusMeters() != null
                        || f.getSort() == SpotSearchRequest.SortBy.DISTANCE;
        if (geo) UiSupport.coordinates(f.getLatitude(), f.getLongitude());
        SpotCategory category = null;
        if (f.getCategory() != null) {
            try {
                category =
                        SpotCategory.valueOf(
                                f.getCategory().toUpperCase(Locale.ROOT).replace('-', '_'));
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
            }
        }
        final SpotCategory selected = category;
        Specification<Spot> spec =
                (s, q, cb) -> {
                    List<Predicate> p = new ArrayList<>();
                    if (selected != null) p.add(cb.equal(s.get("category"), selected));
                    if (f.getRegionId() != null)
                        p.add(cb.equal(s.get("regionId"), f.getRegionId()));
                    if (f.getMinRating() != null)
                        p.add(cb.ge(s.get("avgRating"), f.getMinRating()));
                    if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                        String kw =
                                "%"
                                        + f.getKeyword()
                                                .trim()
                                                .toLowerCase(Locale.ROOT)
                                                .replace("\\", "\\\\")
                                                .replace("%", "\\%")
                                                .replace("_", "\\_")
                                        + "%";
                        p.add(
                                cb.or(
                                        cb.like(cb.lower(s.get("name")), kw, '\\'),
                                        cb.like(cb.lower(s.get("nameEn")), kw, '\\'),
                                        cb.like(cb.lower(s.get("title")), kw, '\\')));
                    }
                    Expression<Double> distance =
                            geo ? distance(s, cb, f.getLatitude(), f.getLongitude()) : null;
                    if (f.getRadiusMeters() != null) p.add(cb.le(distance, f.getRadiusMeters()));
                    if (q.getResultType() != Long.class && q.getResultType() != long.class) {
                        Order primary =
                                switch (f.getSort()) {
                                    case POPULAR ->
                                            cb.desc(
                                                    cb.sum(
                                                            s.<Long>get("savedCount"),
                                                            s.<Long>get("reviewCount")));
                                    case RATING -> cb.desc(s.get("avgRating"));
                                    case LATEST -> cb.desc(s.get("createdAt"));
                                    case DISTANCE -> cb.asc(distance);
                                };
                        q.orderBy(primary, cb.desc(s.get("id")));
                    }
                    return cb.and(p.toArray(Predicate[]::new));
                };
        Page<Spot> page = spots.findAll(spec, PageRequest.of(f.getPage(), f.getSize()));
        return page.map(
                s -> card(s, lang, false, f.getLatitude(), f.getLongitude()));
    }

    // Haversine expression runs in the database, including ordering and pagination.
    private Expression<Double> distance(Root<Spot> s, CriteriaBuilder cb, double lat, double lng) {
        Expression<Double> dlat =
                cb.quot(cb.diff(s.<Double>get("latitude"), lat), 2).as(Double.class);
        Expression<Double> dlng =
                cb.quot(cb.diff(s.<Double>get("longitude"), lng), 2).as(Double.class);
        Expression<Double> sinLat =
                cb.function("sin", Double.class, cb.function("radians", Double.class, dlat));
        Expression<Double> sinLng =
                cb.function("sin", Double.class, cb.function("radians", Double.class, dlng));
        Expression<Double> a =
                cb.sum(
                        cb.prod(sinLat, sinLat),
                        cb.prod(
                                cb.prod(
                                        cb.literal(Math.cos(Math.toRadians(lat))),
                                        cb.function(
                                                "cos",
                                                Double.class,
                                                cb.function(
                                                        "radians",
                                                        Double.class,
                                                        s.get("latitude")))),
                                cb.prod(sinLng, sinLng)));
        Expression<Double> clamped =
                cb.<Double>selectCase()
                        .when(cb.gt(a, 1.0), 1.0)
                        .when(cb.lt(a, 0.0), 0.0)
                        .otherwise(a);
        return cb.prod(12742000.0, cb.function("asin", Double.class, cb.sqrt(clamped)));
    }

    public SpotListResponse card(Spot s, String lang, boolean saved, Double lat, Double lng) {
        return SpotListResponse.builder()
                .spotId(s.getId())
                .regionId(s.getRegionId())
                .name(UiSupport.localized(s.getName(), s.getNameEn(), lang))
                .description(UiSupport.localized(s.getDescription(), s.getDescriptionEn(), lang))
                .category(s.getCategory())
                .categoryCode(UiSupport.categoryCode(s.getCategory()))
                .categoryLabel(UiSupport.categoryLabel(s.getCategory(), lang))
                .address(UiSupport.localized(s.getAddress(), s.getAddressEn(), lang))
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .thumbnailUrl(s.getThumbnailUrl())
                .avgRating(s.getAvgRating())
                .reviewCount(s.getReviewCount())
                .saved(saved)
                .distanceMeters(
                        lat == null || lng == null
                                ? null
                                : UiSupport.meters(
                                        lat,
                                        lng,
                                        s.getLatitude().doubleValue(),
                                        s.getLongitude().doubleValue()))
                .build();
    }
}
