package com.Wavey.WaveyService.domain.notification.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum NotificationType {
    STAMP,
    BADGE,
    ROUTE,
    SPOT,
    REVIEW,
    SYSTEM;

    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static NotificationType from(String value) {
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}
