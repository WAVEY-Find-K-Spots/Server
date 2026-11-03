package com.Wavey.WaveyService.domain.stamp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@DataJpaTest(
        properties = {
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class UserStampRepositoryExistingSpotTest {

    private static final long USER = 1L;
    private static final long OTHER_USER = 2L;
    private static final long MISSING_SPOT_ID = 990001L;

    @Autowired private UserStampRepository userStamps;
    @Autowired private SpotRepository spots;

    @Test
    void recordOfMissingSpot_isExcludedFromCountAndList() {
        Spot spot = saveSpot();
        UserStamp kept = acquire(USER, 1L, spot.getId());
        acquire(USER, 2L, MISSING_SPOT_ID);

        assertThat(userStamps.countWithExistingSpot(USER)).isEqualTo(1);
        assertThat(userStamps.findWithExistingSpot(USER))
                .extracting(UserStamp::getId)
                .containsExactly(kept.getId());
    }

    @Test
    void onlyRecordOfMissingSpot_countsAsZero() {
        acquire(USER, 1L, MISSING_SPOT_ID);

        assertThat(userStamps.countWithExistingSpot(USER)).isZero();
        assertThat(userStamps.findWithExistingSpot(USER)).isEmpty();
    }

    @Test
    void otherUsersRecords_areNotIncluded() {
        Spot spot = saveSpot();
        acquire(OTHER_USER, 1L, spot.getId());

        assertThat(userStamps.countWithExistingSpot(USER)).isZero();
        assertThat(userStamps.findWithExistingSpot(USER)).isEmpty();
        assertThat(userStamps.countWithExistingSpot(OTHER_USER)).isEqualTo(1);
    }

    private Spot saveSpot() {
        return spots.saveAndFlush(
                Spot.builder()
                        .regionId(1L)
                        .nameKo("스팟")
                        .category(SpotCategory.K_DRAMA)
                        .placeType(PlaceType.OTHER)
                        .addressKo("주소")
                        .latitude(new BigDecimal("37.50000000"))
                        .longitude(new BigDecimal("127.00000000"))
                        .build());
    }

    private UserStamp acquire(long userId, long stampId, long spotId) {
        return userStamps.saveAndFlush(
                UserStamp.builder()
                        .userId(userId)
                        .stampId(stampId)
                        .spotId(spotId)
                        .regionId(1L)
                        .acquiredAt(LocalDateTime.of(2026, 9, 1, 12, 0))
                        .build());
    }
}
