package com.Wavey.WaveyService.domain.notification.service;

import com.Wavey.WaveyService.domain.notification.entity.Notification;
import com.Wavey.WaveyService.domain.notification.repository.NotificationRepository;
import com.Wavey.WaveyService.domain.user.entity.UserSettings;
import com.Wavey.WaveyService.domain.user.repository.UserSettingsRepository;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserSettingsRepository settings;

    public record Item(
            Long id,
            String type,
            String title,
            String body,
            String targetType,
            Long targetId,
            LocalDateTime createdAt,
            LocalDateTime readAt) {}

    public record Inbox(long unreadCount, Page<Item> notifications) {}

    public Inbox inbox(Long id, int page, int size, String language) {
        String lang =
                UiSupport.language(
                        language != null
                                ? language
                                : settings.findByUserId(id).map(UserSettings::getLanguage).orElse("ko"));
        return new Inbox(
                notifications.countByUserIdAndReadAtIsNull(id),
                notifications
                        .findByUserId(
                                id,
                                PageRequest.of(
                                        page,
                                        size,
                                        Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                        .map(
                                n ->
                                        new Item(
                                                n.getId(),
                                                n.getType(),
                                                UiSupport.localized(
                                                        n.getTitle(), n.getTitleEn(), lang),
                                                UiSupport.localized(
                                                        n.getBody(), n.getBodyEn(), lang),
                                                n.getTargetType(),
                                                n.getTargetId(),
                                                n.getCreatedAt(),
                                                n.getReadAt())));
    }

    @Transactional
    public void read(Long userId, Long id) {
        var n =
                notifications
                        .findByIdAndUserId(id, userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        if (n.getReadAt() == null) n.setReadAt(LocalDateTime.now());
    }

    @Transactional
    public int readAll(Long id) {
        return notifications.readAll(id, LocalDateTime.now());
    }

    @Transactional
    public void stamp(Long userId, Long stampId, String name, String nameEn) {
        UserSettings s =
                settings.findByUserId(userId)
                        .orElseGet(() -> UserSettings.builder().userId(userId).build());
        if (!s.isStampEnabled()) return;
        notifications.save(
                Notification.builder()
                        .userId(userId)
                        .type("STAMP")
                        .title("스탬프를 획득했어요")
                        .titleEn("Stamp acquired")
                        .body(name + " 방문 스탬프가 추가됐어요.")
                        .bodyEn((nameEn == null ? name : nameEn) + " stamp was added.")
                        .targetType("STAMP")
                        .targetId(stampId)
                        .build());
        // External push delivery intentionally disabled; notifications are persisted in DB only.
        // pushClient.send(...);
    }
}
