package com.Wavey.WaveyService.domain.notification.event;

public sealed interface NotificationEvent {

    record StampAcquired(
            Long userId, Long stampId, String nameKo, String nameEn)
            implements NotificationEvent {}

    record BadgeAcquired(
            Long userId, Long badgeId, String nameKo, String nameEn)
            implements NotificationEvent {}

    record BadgeProgressed(
            Long userId, Long badgeId, long progress, int requiredStamps)
            implements NotificationEvent {}

    record ReviewCreated(
            Long authorUserId,
            Long reviewId,
            Long spotId,
            String spotNameKo,
            String spotNameEn)
            implements NotificationEvent {}
}
