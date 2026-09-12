package com.Wavey.WaveyService.domain.spot.enums;

public enum PlaceType {

    RESTAURANT,
    PLAYGROUND,
    CAFE,
    STAY,
    STATION,
    STORE,
    CVS,
    SHOP,
    OTHER;

    public static PlaceType from(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }

        try {
            return value.trim()
                    .toUpperCase()
                    .replace('-', '_')
                    .replace(' ', '_')
                    .transform(PlaceType::valueOf);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}