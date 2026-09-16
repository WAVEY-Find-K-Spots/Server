package com.Wavey.WaveyService.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.notification.dto.NotificationCreateCommand;
import com.Wavey.WaveyService.domain.notification.dto.NotificationInboxResponse;
import com.Wavey.WaveyService.domain.notification.dto.NotificationSettingsUpdateRequest;
import com.Wavey.WaveyService.domain.notification.dto.SystemNotificationRequest;
import com.Wavey.WaveyService.domain.notification.entity.Notification;
import com.Wavey.WaveyService.domain.notification.enums.NotificationTargetType;
import com.Wavey.WaveyService.domain.notification.enums.NotificationType;
import com.Wavey.WaveyService.domain.notification.repository.NotificationRepository;
import com.Wavey.WaveyService.domain.user.entity.UserSetting;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;
import com.Wavey.WaveyService.domain.user.repository.UserSettingsRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notifications;
    @Mock private UserSettingsRepository settings;
    @Mock private UserRepository users;

    @InjectMocks private NotificationService service;

    @Test
    void inbox_returnsLocalizedItemsAndUnreadCount() {
        Notification notification = notification(1L, null);
        given(notifications.findByUserId(any(), any()))
                .willReturn(new PageImpl<>(List.of(notification)));
        given(notifications.countByUserIdAndReadAtIsNull(10L)).willReturn(1L);

        NotificationInboxResponse result = service.inbox(10L, "en", 0, 20);

        assertThat(result.unreadCount()).isEqualTo(1L);
        assertThat(result.notifications()).hasSize(1);
        assertThat(result.notifications().getFirst().title()).isEqualTo("English title");
        assertThat(result.notifications().getFirst().read()).isFalse();
    }

    @Test
    void create_savesNotificationWhenEnabled() {
        NotificationCreateCommand command = command(NotificationType.STAMP, "stamp:1:acquired");
        given(settings.findByUserId(10L)).willReturn(Optional.empty());
        given(notifications.existsByUserIdAndEventKey(10L, "stamp:1:acquired"))
                .willReturn(false);

        boolean created = service.create(command);

        assertThat(created).isTrue();
        verify(notifications).save(any(Notification.class));
    }

    @Test
    void create_skipsDuplicateEvent() {
        NotificationCreateCommand command = command(NotificationType.STAMP, "stamp:1:acquired");
        given(settings.findByUserId(10L)).willReturn(Optional.empty());
        given(notifications.existsByUserIdAndEventKey(10L, "stamp:1:acquired"))
                .willReturn(true);

        boolean created = service.create(command);

        assertThat(created).isFalse();
        verify(notifications, never()).save(any());
    }

    @Test
    void createAll_supportsMultipleRecipients() {
        NotificationCreateCommand first = command(NotificationType.SYSTEM, "system:1");
        NotificationCreateCommand second =
                new NotificationCreateCommand(
                        20L,
                        NotificationType.SYSTEM,
                        "제목",
                        "Title",
                        "내용",
                        "Body",
                        NotificationTargetType.SYSTEM,
                        null,
                        "system:1");
        given(settings.findByUserId(any())).willReturn(Optional.empty());
        given(notifications.existsByUserIdAndEventKey(any(), any())).willReturn(false);

        int created = service.createAll(List.of(first, second));

        assertThat(created).isEqualTo(2);
        verify(notifications, org.mockito.Mockito.times(2)).save(any(Notification.class));
    }

    @Test
    void create_skipsDisabledCategory() {
        UserSetting setting =
                UserSetting.builder().userId(10L).spotEnabled(false).build();
        given(settings.findByUserId(10L)).willReturn(Optional.of(setting));

        boolean created = service.create(command(NotificationType.REVIEW, "review:1"));

        assertThat(created).isFalse();
        verify(notifications, never()).existsByUserIdAndEventKey(any(), any());
        verify(notifications, never()).save(any());
    }

    @Test
    void read_rejectsAnotherUsersNotification() {
        given(notifications.findByIdAndUserId(99L, 10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.read(10L, 99L, "ko"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void updateSettings_changesOnlyProvidedFields() {
        UserSetting setting =
                UserSetting.builder()
                        .userId(10L)
                        .pushEnabled(true)
                        .stampEnabled(true)
                        .routeEnabled(true)
                        .spotEnabled(true)
                        .noticeEnabled(true)
                        .build();
        given(settings.findByUserId(10L)).willReturn(Optional.of(setting));
        given(settings.save(setting)).willReturn(setting);

        var result =
                service.updateSettings(
                        10L,
                        new NotificationSettingsUpdateRequest(
                                null, false, null, false, null));

        assertThat(result.pushEnabled()).isTrue();
        assertThat(result.stampEnabled()).isFalse();
        assertThat(result.routeEnabled()).isTrue();
        assertThat(result.spotEnabled()).isFalse();
        assertThat(result.noticeEnabled()).isTrue();
    }

    @Test
    void createSystemNotification_targetsSelectedUsersAndReportsDuplicates() {
        Set<Long> recipients = Set.of(10L, 20L);
        given(users.findExistingIds(recipients)).willReturn(List.of(10L, 20L));
        given(settings.findByUserId(any())).willReturn(Optional.empty());
        given(notifications.existsByUserIdAndEventKey(10L, "system:maintenance-1"))
                .willReturn(false);
        given(notifications.existsByUserIdAndEventKey(20L, "system:maintenance-1"))
                .willReturn(true);

        var result =
                service.createSystemNotification(
                        new SystemNotificationRequest(
                                "점검 안내",
                                "Maintenance",
                                "서비스 점검 예정입니다.",
                                "Maintenance is scheduled.",
                                recipients,
                                NotificationTargetType.SYSTEM,
                                null,
                                "maintenance-1"));

        assertThat(result.requestedCount()).isEqualTo(2);
        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);
    }

    private NotificationCreateCommand command(NotificationType type, String eventKey) {
        return new NotificationCreateCommand(
                10L,
                type,
                "제목",
                "Title",
                "내용",
                "Body",
                NotificationTargetType.SPOT,
                1L,
                eventKey);
    }

    private Notification notification(Long id, LocalDateTime readAt) {
        Notification notification =
                Notification.builder()
                        .userId(10L)
                        .type(NotificationType.SYSTEM)
                        .titleKo("한국어 제목")
                        .titleEn("English title")
                        .bodyKo("한국어 내용")
                        .bodyEn("English body")
                        .eventKey("system:test")
                        .readAt(readAt)
                        .build();
        ReflectionTestUtils.setField(notification, "id", id);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());
        return notification;
    }
}
