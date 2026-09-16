package com.Wavey.WaveyService.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.Wavey.WaveyService.domain.notification.entity.Notification;
import com.Wavey.WaveyService.domain.notification.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityManager;

@DataJpaTest(
        properties = {
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class NotificationRepositoryConstraintTest {

    @Autowired private NotificationRepository notifications;
    @Autowired private EntityManager entityManager;

    @Test
    void sameEventKey_canBeStoredForDifferentUsers() {
        notifications.saveAndFlush(notification(1L, "system:maintenance"));
        notifications.saveAndFlush(notification(2L, "system:maintenance"));

        assertThat(notifications.findAll()).hasSize(2);
    }

    @Test
    void sameEventKey_cannotBeStoredTwiceForOneUser() {
        notifications.saveAndFlush(notification(1L, "stamp:10:acquired"));

        assertThatThrownBy(
                        () ->
                                notifications.saveAndFlush(
                                        notification(1L, "stamp:10:acquired")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void legacyNotificationTypes_areConvertedWithoutDataMigration() {
        entityManager
                .createNativeQuery(
                        "insert into notifications "
                                + "(user_id, type, title, body, created_at) "
                                + "values (1, 'ROUTE_REMINDER', '기존 제목', '기존 내용', current_timestamp)")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        Notification saved = notifications.findAll().getFirst();

        assertThat(saved.getType()).isEqualTo(NotificationType.ROUTE);
        assertThat(saved.getTitleKo()).isEqualTo("기존 제목");
        assertThat(saved.getBodyKo()).isEqualTo("기존 내용");
        assertThat(saved.getEventKey()).isNull();
    }

    private Notification notification(Long userId, String eventKey) {
        return Notification.builder()
                .userId(userId)
                .type(NotificationType.SYSTEM)
                .titleKo("제목")
                .bodyKo("내용")
                .eventKey(eventKey)
                .build();
    }
}
