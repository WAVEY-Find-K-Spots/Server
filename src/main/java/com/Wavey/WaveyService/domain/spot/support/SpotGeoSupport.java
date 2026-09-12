package com.Wavey.WaveyService.domain.spot.support;

import com.Wavey.WaveyService.domain.spot.entity.Spot;

import java.math.BigDecimal;

public final class SpotGeoSupport {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final double METERS_PER_LATITUDE_DEGREE = 111_320.0;

    private SpotGeoSupport() {}

    public static Bounds bounds(
            BigDecimal latitude,
            BigDecimal longitude,
            double radiusMeters
    ) {
        double lat = latitude.doubleValue();
        double lng = longitude.doubleValue();

        double latDelta =
                radiusMeters
                        / METERS_PER_LATITUDE_DEGREE;

        double longitudeMeters =
                METERS_PER_LATITUDE_DEGREE
                        * Math.cos(
                        Math.toRadians(lat)
                );

        double lngDelta =
                radiusMeters
                        / Math.max(longitudeMeters, 1.0);

        return new Bounds(
                lat - latDelta,
                lat + latDelta,
                lng - lngDelta,
                lng + lngDelta
        );
    }

    public static Double meters(
            BigDecimal latitude,
            BigDecimal longitude,
            Spot spot
    ) {
        if (latitude == null || longitude == null) {
            return null;
        }

        double lat1 =
                Math.toRadians(latitude.doubleValue());

        double lon1 =
                Math.toRadians(longitude.doubleValue());

        double lat2 =
                Math.toRadians(
                        spot.getLatitude().doubleValue()
                );

        double lon2 =
                Math.toRadians(
                        spot.getLongitude().doubleValue()
                );

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a =
                Math.pow(
                        Math.sin(dLat / 2.0),
                        2.0
                )
                        + Math.cos(lat1)
                        * Math.cos(lat2)
                        * Math.pow(
                        Math.sin(dLon / 2.0),
                        2.0
                );

        return EARTH_RADIUS_METERS
                * 2.0
                * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1.0 - a)
        );
    }

    public record Bounds(
            double minLat,
            double maxLat,
            double minLng,
            double maxLng
    ) {}
}