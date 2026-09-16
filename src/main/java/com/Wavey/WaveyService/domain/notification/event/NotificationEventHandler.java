package com.Wavey.WaveyService.domain.notification.event;

import com.Wavey.WaveyService.domain.notification.service.NotificationService;
import com.Wavey.WaveyService.domain.spot.repository.SavedSpotRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationService notificationService;
    private final SavedSpotRepository savedSpots;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStampAcquired(NotificationEvent.StampAcquired event) {
        notificationService.notifyStamp(
                event.userId(), event.stampId(), event.nameKo(), event.nameEn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBadgeAcquired(NotificationEvent.BadgeAcquired event) {
        notificationService.notifyBadge(
                event.userId(), event.badgeId(), event.nameKo(), event.nameEn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBadgeProgressed(NotificationEvent.BadgeProgressed event) {
        notificationService.notifyBadgeProgress(
                event.userId(),
                event.badgeId(),
                event.progress(),
                event.requiredStamps());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(NotificationEvent.ReviewCreated event) {
        List<Long> recipients =
                savedSpots.findUserIdsBySpotIdExcludingUser(
                        event.spotId(), event.authorUserId());
        notificationService.notifySavedSpotReview(
                recipients,
                event.reviewId(),
                event.spotId(),
                event.spotNameKo(),
                event.spotNameEn());
    }
}
