package com.Wavey.WaveyService.domain.spot.specification;

import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.SpotContent;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.entity.SavedSpot;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.SortBy;
import com.Wavey.WaveyService.domain.spot.support.SpotGeoSupport;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SpotSearchSpecification {

    private SpotSearchSpecification() {}

    public static Specification<Spot> from(
            SpotSearchRequest request,
            Long userId
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            addRegion(
                    request,
                    root,
                    cb,
                    predicates
            );

            addSavedOnly(
                    request,
                    userId,
                    root,
                    query,
                    cb,
                    predicates
            );

            addCategory(
                    request,
                    root,
                    cb,
                    predicates
            );

            addPlaceType(
                    request,
                    root,
                    cb,
                    predicates
            );

            addRating(
                    request,
                    root,
                    cb,
                    predicates
            );

            addKeyword(
                    request,
                    root,
                    query,
                    cb,
                    predicates
            );

            addExcludeRouteId(
                    request,
                    root,
                    query,
                    cb,
                    predicates
            );

            Expression<Double> distance = null;

            if (hasCoordinates(request)) {
                distance =
                        distanceExpression(
                                root,
                                cb,
                                request.latitude(),
                                request.longitude()
                        );
            }

            if (request.radiusMeters() != null) {
                addBoundingBox(
                        request,
                        root,
                        cb,
                        predicates
                );

                predicates.add(
                        cb.le(
                                distance,
                                request.radiusMeters()
                        )
                );
            }

            if (!isCountQuery(query)) {
                applyOrdering(request, root, query, cb, distance);
            }

            return cb.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }

    /**
     * 정렬 조건 전체를 여기서 한 번에 구성한다(Pageable의 Sort는 쓰지 않음 — {@code SpotSearchService}에서
     * 항상 {@code Sort.unsorted()}를 넘긴다). 이미지 유무를 최우선 기준으로 두고, 그 안에서 기존 정렬을 적용한다.
     */
    private static void applyOrdering(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            Expression<Double> distance
    ) {
        Expression<Integer> hasNoImage = cb.<Integer>selectCase()
                .when(cb.and(
                        cb.isNotNull(root.get("imageUrl")),
                        cb.notEqual(root.get("imageUrl"), "")
                ), 0)
                .otherwise(1);

        List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        orders.add(cb.asc(hasNoImage));

        switch (request.sort()) {
            case POPULAR -> {
                orders.add(cb.desc(root.get("savedCount")));
                orders.add(cb.desc(root.get("reviewCount")));
                orders.add(cb.desc(root.get("id")));
            }
            case RATING -> {
                orders.add(cb.desc(root.<Double>get("avgRating")));
                orders.add(cb.desc(root.get("reviewCount")));
                orders.add(cb.desc(root.get("id")));
            }
            case LATEST -> {
                orders.add(cb.desc(root.get("createdAt")));
                orders.add(cb.desc(root.get("id")));
            }
            case DISTANCE -> {
                orders.add(cb.asc(distance));
                orders.add(cb.desc(root.get("id")));
            }
        }

        query.orderBy(orders);
    }

    private static void addRegion(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (request.regionId() != null) {
            predicates.add(
                    cb.equal(
                            root.get("regionId"),
                            request.regionId()
                    )
            );
        }
    }

    private static void addCategory(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (request.category() != null) {
            predicates.add(
                    cb.equal(
                            root.get("category"),
                            request.category()
                    )
            );
        }
    }

    private static void addPlaceType(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (request.placeType() != null) {
            predicates.add(
                    cb.equal(
                            root.get("placeType"),
                            request.placeType()
                    )
            );
        }
    }

    private static void addRating(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (request.minRating() != null) {
            predicates.add(
                    cb.ge(
                            root.<Double>get("avgRating"),
                            request.minRating()
                    )
            );
        }
    }

    private static void addKeyword(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (request.keyword() == null
                || request.keyword().isBlank()) {
            return;
        }

        String pattern =
                "%"
                        + escapeLike(
                        request.keyword()
                                .trim()
                                .toLowerCase(Locale.ROOT)
                )
                        + "%";

        Subquery<Integer> exists =
                query.subquery(Integer.class);

        Root<SpotContent> spotContent =
                exists.from(SpotContent.class);

        Root<Content> content =
                exists.from(Content.class);

        exists.select(cb.literal(1));

        exists.where(
                cb.equal(
                        spotContent.get("spotId"),
                        root.get("id")
                ),
                cb.equal(
                        spotContent.get("contentId"),
                        content.get("id")
                ),
                cb.or(
                        cb.like(
                                cb.lower(
                                        content.<String>get("titleKo")
                                ),
                                pattern,
                                '\\'
                        ),
                        cb.like(
                                cb.lower(
                                        content.<String>get("titleEn")
                                ),
                                pattern,
                                '\\'
                        )
                )
        );

        predicates.add(
                cb.or(
                        cb.like(
                                cb.lower(
                                        root.<String>get("nameKo")
                                ),
                                pattern,
                                '\\'
                        ),
                        cb.like(
                                cb.lower(
                                        root.<String>get("nameEn")
                                ),
                                pattern,
                                '\\'
                        ),
                        cb.exists(exists)
                )
        );
    }

    private static void addSavedOnly(
            SpotSearchRequest request,
            Long userId,
            Root<Spot> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (!Boolean.TRUE.equals(request.savedOnly())) {
            return;
        }

        if (userId == null) {
            predicates.add(cb.disjunction());
            return;
        }

        Subquery<Integer> exists =
                query.subquery(Integer.class);

        Root<SavedSpot> savedSpot =
                exists.from(SavedSpot.class);

        exists.select(cb.literal(1));

        exists.where(
                cb.equal(savedSpot.get("userId"), userId),
                cb.equal(savedSpot.get("spotId"), root.get("id"))
        );

        predicates.add(cb.exists(exists));
    }

    private static void addExcludeRouteId(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        if (request.excludeRouteId() == null) {
            return;
        }

        Subquery<Integer> exists =
                query.subquery(Integer.class);

        Root<RouteSpot> routeSpot =
                exists.from(RouteSpot.class);

        exists.select(cb.literal(1));

        exists.where(
                cb.equal(
                        routeSpot.get("route").get("id"),
                        request.excludeRouteId()
                ),
                cb.equal(
                        routeSpot.get("spotId"),
                        root.get("id")
                )
        );

        predicates.add(cb.not(cb.exists(exists)));
    }

    private static void addBoundingBox(
            SpotSearchRequest request,
            Root<Spot> root,
            CriteriaBuilder cb,
            List<Predicate> predicates
    ) {
        SpotGeoSupport.Bounds bounds =
                SpotGeoSupport.bounds(
                        request.latitude(),
                        request.longitude(),
                        request.radiusMeters()
                );

        predicates.add(
                cb.between(
                        root.<BigDecimal>get("latitude"),
                        BigDecimal.valueOf(bounds.minLat()),
                        BigDecimal.valueOf(bounds.maxLat())
                )
        );

        predicates.add(
                cb.between(
                        root.<BigDecimal>get("longitude"),
                        BigDecimal.valueOf(bounds.minLng()),
                        BigDecimal.valueOf(bounds.maxLng())
                )
        );
    }

    private static Expression<Double> distanceExpression(
            Root<Spot> root,
            CriteriaBuilder cb,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        double lat = latitude.doubleValue();
        double lng = longitude.doubleValue();

        Expression<Double> spotLat =
                cb.toDouble(
                        root.<BigDecimal>get("latitude")
                );

        Expression<Double> spotLng =
                cb.toDouble(
                        root.<BigDecimal>get("longitude")
                );

        Expression<Double> dLat =
                cb.quot(
                        cb.diff(
                                spotLat.as(Double.class),
                                lat
                        ),
                        2.0
                ).as(Double.class);

        Expression<Double> dLng =
                cb.quot(
                        cb.diff(
                                spotLng.as(Double.class),
                                lng
                        ),
                        2.0
                ).as(Double.class);

        Expression<Double> sinLat =
                cb.function(
                        "sin",
                        Double.class,
                        cb.function(
                                "radians",
                                Double.class,
                                dLat
                        )
                );

        Expression<Double> sinLng =
                cb.function(
                        "sin",
                        Double.class,
                        cb.function(
                                "radians",
                                Double.class,
                                dLng
                        )
                );

        Expression<Double> a =
                cb.sum(
                        cb.prod(
                                sinLat,
                                sinLat
                        ),
                        cb.prod(
                                cb.prod(
                                        cb.literal(
                                                Math.cos(
                                                        Math.toRadians(lat)
                                                )
                                        ),
                                        cb.function(
                                                "cos",
                                                Double.class,
                                                cb.function(
                                                        "radians",
                                                        Double.class,
                                                        spotLat
                                                )
                                        )
                                ),
                                cb.prod(
                                        sinLng,
                                        sinLng
                                )
                        )
                );

        Expression<Double> clamped =
                cb.<Double>selectCase()
                        .when(cb.gt(a, 1.0), 1.0)
                        .when(cb.lt(a, 0.0), 0.0)
                        .otherwise(a);

        return cb.prod(
                12_742_000.0,
                cb.function(
                        "asin",
                        Double.class,
                        cb.sqrt(clamped)
                )
        );
    }

    private static boolean hasCoordinates(
            SpotSearchRequest request
    ) {
        return request.latitude() != null
                && request.longitude() != null;
    }

    private static boolean isCountQuery(
            CriteriaQuery<?> query
    ) {
        return query.getResultType() == Long.class
                || query.getResultType() == long.class;
    }

    private static String escapeLike(
            String value
    ) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
