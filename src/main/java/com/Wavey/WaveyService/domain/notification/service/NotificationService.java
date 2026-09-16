package com.Wavey.WaveyService.domain.notification.service;

import com.Wavey.WaveyService.domain.notification.dto.NotificationCreateCommand;
import com.Wavey.WaveyService.domain.notification.dto.NotificationInboxResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationSettingsResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationSettingsUpdateRequest;
import com.Wavey.WaveyService.domain.notification.dto.SystemNotificationRequest;
import com.Wavey.WaveyService.domain.notification.dto.SystemNotificationResult;
import com.Wavey.WaveyService.domain.notification.entity.Notification;
import com.Wavey.WaveyService.domain.notification.enums.NotificationTargetType;
import com.Wavey.WaveyService.domain.notification.enums.NotificationType;
import com.Wavey.WaveyService.domain.notification.repository.NotificationRepository;
import com.Wavey.WaveyService.domain.user.entity.UserSetting;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.repository.UserSettingsRepository;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notifications;
    private final UserSettingsRepository settings;
    private final UserRepository users;

    public NotificationInboxResponse inbox(
            Long userId, String language, Integer page, Integer size) {
        String lang = UiSupport.language(language);
        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = normalizeSize(size);
        PageRequest pageable =
                PageRequest.of(
                        pageNumber,
                        pageSize,
                        Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<Notification> result = notifications.findByUserId(userId, pageable);
        List<NotificationResponse> items =
                result.getContent().stream().map(item -> toResponse(item, lang)).toList();

        return new NotificationInboxResponse(
                notifications.countByUserIdAndReadAtIsNull(userId),
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext());
    }

    @Transactional
    public NotificationResponse read(Long userId, Long notificationId, String language) {
        Notification notification =
                notifications
                        .findByIdAndUserId(notificationId, userId)
                        .orElseThrow(
                                () -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.markRead(LocalDateTime.now());
        return toResponse(notification, UiSupport.language(language));
    }

    @Transactional
    public int readAll(Long userId) {
        return notifications.markAllRead(userId, LocalDateTime.now());
    }

    public NotificationSettingsResponse getSettings(Long userId) {
        return settings
                .findByUserId(userId)
                .map(this::toSettingsResponse)
                .orElseGet(this::defaultSettings);
    }

    @Transactional
    public NotificationSettingsResponse updateSettings(
            Long userId, NotificationSettingsUpdateRequest request) {
        UserSetting setting =
                settings
                        .findByUserId(userId)
                        .orElseGet(
                                () ->
                                        UserSetting.builder()
                                                .userId(userId)
                                                .build());

        if (request.pushEnabled() != null) {
            setting.setPushEnabled(request.pushEnabled());
        }
        if (request.stampEnabled() != null) {
            setting.setStampEnabled(request.stampEnabled());
        }
        if (request.routeEnabled() != null) {
            setting.setRouteEnabled(request.routeEnabled());
        }
        if (request.spotEnabled() != null) {
            setting.setSpotEnabled(request.spotEnabled());
        }
        if (request.noticeEnabled() != null) {
            setting.setNoticeEnabled(request.noticeEnabled());
        }

        return toSettingsResponse(settings.save(setting));
    }

    @Transactional
    public boolean create(NotificationCreateCommand command) {
        validate(command);
        if (!isEnabled(command.userId(), command.type())) {
            return false;
        }
        if (notifications.existsByUserIdAndEventKey(command.userId(), command.eventKey())) {
            return false;
        }

        notifications.save(
                Notification.builder()
                        .userId(command.userId())
                        .type(command.type())
                        .titleKo(command.titleKo())
                        .titleEn(command.titleEn())
                        .bodyKo(command.bodyKo())
                        .bodyEn(command.bodyEn())
                        .targetType(command.targetType())
                        .targetId(command.targetId())
                        .eventKey(command.eventKey())
                        .build());
        return true;
    }

    @Transactional
    public int createAll(List<NotificationCreateCommand> commands) {
        if (commands == null) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
        int created = 0;
        for (NotificationCreateCommand command : commands) {
            if (create(command)) {
                created++;
            }
        }
        return created;
    }

    @Transactional
    public SystemNotificationResult createSystemNotification(SystemNotificationRequest request) {
        Set<Long> recipientIds = resolveRecipients(request.recipientUserIds());
        int created = 0;

        for (Long userId : recipientIds) {
            if (create(
                    new NotificationCreateCommand(
                            userId,
                            NotificationType.SYSTEM,
                            request.title(),
                            request.titleEn(),
                            request.body(),
                            request.bodyEn(),
                            request.targetType(),
                            request.targetId(),
                            "system:" + request.eventKey()))) {
                created++;
            }
        }

        return new SystemNotificationResult(
                recipientIds.size(), created, recipientIds.size() - created);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyStamp(
            Long userId, Long stampId, String nameKo, String nameEn) {
        create(
                new NotificationCreateCommand(
                        userId,
                        NotificationType.STAMP,
                        "새 스탬프를 획득했어요",
                        "New stamp collected",
                        nameKo + " 스탬프가 스탬프북에 추가되었습니다.",
                        displayName(nameEn, nameKo) + " was added to your stamp book.",
                        NotificationTargetType.STAMP,
                        stampId,
                        "stamp:" + stampId + ":acquired"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyBadge(Long userId, Long badgeId, String nameKo, String nameEn) {
        create(
                new NotificationCreateCommand(
                        userId,
                        NotificationType.BADGE,
                        "새 배지를 획득했어요",
                        "New badge earned",
                        nameKo + " 배지를 획득했습니다.",
                        "You earned the " + displayName(nameEn, nameKo) + " badge.",
                        NotificationTargetType.BADGE,
                        badgeId,
                        "badge:" + badgeId + ":acquired"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyBadgeProgress(
            Long userId, Long badgeId, long progress, int requiredStamps) {
        create(
                new NotificationCreateCommand(
                        userId,
                        NotificationType.BADGE,
                        "배지 진행률이 올랐어요",
                        "Badge progress updated",
                        "배지 달성까지 " + progress + "/" + requiredStamps + "개를 모았습니다.",
                        "Badge progress is now " + progress + "/" + requiredStamps + ".",
                        NotificationTargetType.BADGE,
                        badgeId,
                        "badge:" + badgeId + ":progress:" + progress));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifySavedSpotReview(
            List<Long> userIds,
            Long reviewId,
            Long spotId,
            String spotNameKo,
            String spotNameEn) {
        for (Long userId : userIds) {
            create(
                    new NotificationCreateCommand(
                            userId,
                            NotificationType.REVIEW,
                            "저장한 장소에 새 리뷰가 등록됐어요",
                            "New review for a saved spot",
                            spotNameKo + "에 새 리뷰가 등록되었습니다.",
                            "A new review was posted for "
                                    + displayName(spotNameEn, spotNameKo)
                                    + ".",
                            NotificationTargetType.SPOT,
                            spotId,
                            "review:" + reviewId + ":saved-spot"));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean notifyRouteRecommendation(
            Long userId,
            Long routeId,
            String routeNameKo,
            String routeNameEn,
            String eventKey) {
        return create(
                new NotificationCreateCommand(
                        userId,
                        NotificationType.ROUTE,
                        "새로운 추천 루트가 도착했어요",
                        "A new route is ready",
                        displayName(routeNameKo, "추천 루트") + "를 확인해 보세요.",
                        "Check out " + displayName(routeNameEn, "your recommended route") + ".",
                        NotificationTargetType.ROUTE,
                        routeId,
                        scopedEventKey("route:", eventKey)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean notifySpotUpdate(
            Long userId,
            Long spotId,
            String titleKo,
            String titleEn,
            String bodyKo,
            String bodyEn,
            String eventKey) {
        return create(
                new NotificationCreateCommand(
                        userId,
                        NotificationType.SPOT,
                        titleKo,
                        titleEn,
                        bodyKo,
                        bodyEn,
                        NotificationTargetType.SPOT,
                        spotId,
                        scopedEventKey("spot:", eventKey)));
    }

    private Set<Long> resolveRecipients(Set<Long> requestedUserIds) {
        if (requestedUserIds == null || requestedUserIds.isEmpty()) {
            return new LinkedHashSet<>(users.findAllIds());
        }

        Set<Long> requested = new LinkedHashSet<>(requestedUserIds);
        Set<Long> existing = new LinkedHashSet<>(users.findExistingIds(requested));
        if (!existing.equals(requested)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return existing;
    }

    private boolean isEnabled(Long userId, NotificationType type) {
        return settings
                .findByUserId(userId)
                .map(setting -> enabledBySetting(setting, type))
                .orElse(true);
    }

    private boolean enabledBySetting(UserSetting setting, NotificationType type) {
        if (!setting.isPushEnabled()) {
            return false;
        }
        return switch (type) {
            case STAMP, BADGE -> setting.isStampEnabled();
            case ROUTE -> setting.isRouteEnabled();
            case SPOT, REVIEW -> setting.isSpotEnabled();
            case SYSTEM -> setting.isNoticeEnabled();
        };
    }

    private NotificationResponse toResponse(Notification notification, String language) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                UiSupport.localized(
                        notification.getTitleKo(), notification.getTitleEn(), language),
                UiSupport.localized(
                        notification.getBodyKo(), notification.getBodyEn(), language),
                notification.getTargetType(),
                notification.getTargetId(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt());
    }

    private NotificationSettingsResponse toSettingsResponse(UserSetting setting) {
        return new NotificationSettingsResponse(
                setting.isPushEnabled(),
                setting.isStampEnabled(),
                setting.isRouteEnabled(),
                setting.isSpotEnabled(),
                setting.isNoticeEnabled());
    }

    private NotificationSettingsResponse defaultSettings() {
        return new NotificationSettingsResponse(true, true, true, true, true);
    }

    private void validate(NotificationCreateCommand command) {
        if (command == null
                || command.userId() == null
                || command.type() == null
                || !hasText(command.titleKo())
                || !hasText(command.bodyKo())
                || !hasText(command.eventKey())) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String displayName(String preferred, String fallback) {
        return hasText(preferred) ? preferred : fallback;
    }

    private String scopedEventKey(String prefix, String eventKey) {
        if (!hasText(eventKey)) {
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        }
        return prefix + eventKey;
    }
}
