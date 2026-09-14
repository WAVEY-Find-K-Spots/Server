package com.Wavey.WaveyService.global.common;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.global.exception.*;

import java.util.*;

public final class UiSupport {
    private UiSupport() {}

    public static String language(String value) {
        if (value == null) return "ko";
        if (!Set.of("ko", "en").contains(value))
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        return value;
    }

    public static String localized(String ko, String en, String language) {
        return "en".equals(language) && en != null && !en.isBlank() ? en : ko;
    }

    public static String categoryCode(SpotCategory c) {
        return c.name().replace('_', '-');
    }

    public static String categoryLabel(SpotCategory c, String language) {
        if ("en".equals(language)) return categoryCode(c);
        return switch (c) {
            case K_DRAMA -> "드라마";
            case K_POP -> "아이돌";
            case K_MOVIE -> "영화";
            case K_HERITAGE -> "관광지";
        };
    }

    public static void coordinates(Double lat, Double lng) {
        if (lat == null
                || lng == null
                || !Double.isFinite(lat)
                || !Double.isFinite(lng)
                || Math.abs(lat) > 90
                || Math.abs(lng) > 180)
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
    }

    public static double meters(double a, double b, double c, double d) {
        double x =
                Math.pow(Math.sin(Math.toRadians(c - a) / 2), 2)
                        + Math.cos(Math.toRadians(a))
                                * Math.cos(Math.toRadians(c))
                                * Math.pow(Math.sin(Math.toRadians(d - b) / 2), 2);
        return 6371000 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, x))));
    }
}
