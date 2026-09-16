package com.Wavey.WaveyService.domain.notification.event;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.Wavey.WaveyService.domain.notification.service.NotificationService;
import com.Wavey.WaveyService.domain.spot.repository.SavedSpotRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock private NotificationService notificationService;
    @Mock private SavedSpotRepository savedSpots;

    @InjectMocks private NotificationEventHandler handler;

    @Test
    void reviewCreated_notifiesUsersWhoSavedTheSpotExceptAuthor() {
        given(savedSpots.findUserIdsBySpotIdExcludingUser(3L, 7L))
                .willReturn(List.of(10L, 20L));

        handler.onReviewCreated(
                new NotificationEvent.ReviewCreated(
                        7L, 11L, 3L, "경복궁", "Gyeongbokgung Palace"));

        verify(notificationService)
                .notifySavedSpotReview(
                        List.of(10L, 20L),
                        11L,
                        3L,
                        "경복궁",
                        "Gyeongbokgung Palace");
    }

    @Test
    void stampAcquired_delegatesToCommonNotificationService() {
        handler.onStampAcquired(
                new NotificationEvent.StampAcquired(
                        7L, 2L, "남산서울타워", "N Seoul Tower"));

        verify(notificationService)
                .notifyStamp(7L, 2L, "남산서울타워", "N Seoul Tower");
    }

    @Test
    void badgeProgressed_delegatesToCommonNotificationService() {
        handler.onBadgeProgressed(
                new NotificationEvent.BadgeProgressed(7L, 3L, 2L, 5));

        verify(notificationService).notifyBadgeProgress(7L, 3L, 2L, 5);
    }
}
