package com.Wavey.WaveyService.domain.notification.converter;

import com.Wavey.WaveyService.domain.notification.enums.NotificationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public NotificationType convertToEntityAttribute(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        return switch (databaseValue) {
            case "ROUTE_REMINDER" -> NotificationType.ROUTE;
            case "SPOT_RECOMMENDATION" -> NotificationType.SPOT;
            default -> NotificationType.valueOf(databaseValue);
        };
    }
}
