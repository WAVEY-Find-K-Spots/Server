package com.Wavey.WaveyService.domain.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.entity.Badge;
import com.Wavey.WaveyService.domain.stamp.entity.Stamp;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeRepository;
import com.Wavey.WaveyService.domain.stamp.repository.BadgeSpotRepository;
import com.Wavey.WaveyService.domain.stamp.repository.StampRepository;
import com.Wavey.WaveyService.domain.stamp.repository.UserBadgeRepository;
import com.Wavey.WaveyService.domain.stamp.repository.UserStampRepository;
import com.Wavey.WaveyService.domain.upload.service.UploadService;
import com.Wavey.WaveyService.domain.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StampCategoryResponseTest {

    private static final long USER = 1L;

    @Mock private StampRepository stamps;
    @Mock private UserStampRepository collected;
    @Mock private BadgeRepository badges;
    @Mock private BadgeSpotRepository badgeSpots;
    @Mock private UserBadgeRepository awards;
    @Mock private UserRepository users;
    @Mock private SpotRepository spots;
    @Mock private UploadService uploadService;
    @Mock private UserBadgeService userBadges;
    @Mock private ApplicationEventPublisher eventPublisher;

    @Test
    void stampItem_hasCategoryOfSpot() {
        Spot spot =
                Spot.builder()
                        .regionId(1L)
                        .nameKo("경복궁")
                        .category(SpotCategory.K_HERITAGE)
                        .placeType(PlaceType.OTHER)
                        .addressKo("주소")
                        .latitude(new BigDecimal("37.50000000"))
                        .longitude(new BigDecimal("127.00000000"))
                        .build();
        given(stamps.findById(5L))
                .willReturn(Optional.of(Stamp.builder().spotId(10L).build()));
        given(spots.findById(10L)).willReturn(Optional.of(spot));
        given(collected.findByUserIdAndStampId(USER, 5L)).willReturn(Optional.empty());
        StampService service =
                new StampService(
                        stamps, collected, badgeSpots, users, spots, userBadges, eventPublisher);

        StampService.StampItem item = service.detail(5L, USER, "ko");

        assertThat(item.category()).isEqualTo(SpotCategory.K_HERITAGE);
    }

    @Test
    void badgeItem_hasCategoryOfBadge_orNullWithoutCondition() {
        given(collected.findWithExistingSpot(USER)).willReturn(List.of());
        given(badges.findAll())
                .willReturn(
                        List.of(badge(1L, SpotCategory.K_POP), badge(2L, null)));
        given(awards.findByUserId(USER)).willReturn(List.of());
        UserBadgeService service =
                new UserBadgeService(
                        badges,
                        badgeSpots,
                        awards,
                        collected,
                        users,
                        spots,
                        uploadService,
                        eventPublisher);

        UserBadgeService.BadgeCollection result = service.collection(USER, "ko");

        assertThat(result.inProgress())
                .extracting(UserBadgeService.BadgeItem::badgeId, UserBadgeService.BadgeItem::category)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, SpotCategory.K_POP),
                        org.assertj.core.groups.Tuple.tuple(2L, null));
    }

    private Badge badge(long id, SpotCategory category) {
        Badge badge =
                Badge.builder().name("뱃지" + id).requiredStamps(3).category(category).build();
        ReflectionTestUtils.setField(badge, "id", id);
        return badge;
    }
}
