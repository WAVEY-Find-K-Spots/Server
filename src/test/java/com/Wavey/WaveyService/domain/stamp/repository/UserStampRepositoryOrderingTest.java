package com.Wavey.WaveyService.domain.stamp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.domain.stamp.entity.Stamp;
import com.Wavey.WaveyService.domain.stamp.entity.UserStamp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest(
        properties = {
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class UserStampRepositoryOrderingTest {

    private static final long USER = 1L;
    private static final long OTHER_USER = 2L;

    @Autowired private UserStampRepository userStamps;
    @Autowired private StampRepository stamps;
    @Autowired private SpotRepository spots;

    @Test
    void acquiredSpotsComeFirst_newestAcquisitionFirst_thenUnacquiredById() {
        List<Spot> saved = saveSpots(1L, 1L, 1L, 1L, 1L);
        Spot s1 = saved.get(0);
        Spot s2 = saved.get(1);
        Spot s3 = saved.get(2);
        Spot s4 = saved.get(3);
        Spot s5 = saved.get(4);

        LocalDateTime base = LocalDateTime.of(2026, 9, 1, 12, 0);
        acquire(USER, s5, base);
        acquire(USER, s3, base.plusDays(1));

        Page<Spot> page = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Spot::getId)
                .containsExactly(s3.getId(), s5.getId(), s1.getId(), s2.getId(), s4.getId());
    }

    @Test
    void acquiredSpotWithLargestId_appearsOnFirstPage() {
        List<Spot> saved = saveSpots(1L, 1L, 1L, 1L, 1L, 1L);
        Spot last = saved.get(saved.size() - 1);
        acquire(USER, last, LocalDateTime.of(2026, 9, 1, 12, 0));

        Page<Spot> firstPage = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(0, 2));

        assertThat(firstPage.getContent().get(0).getId()).isEqualTo(last.getId());
        assertThat(firstPage.getContent()).hasSize(2);
    }

    @Test
    void paging_keepsTotalsAndDoesNotRepeatOrSkipSpots() {
        List<Spot> saved = saveSpots(1L, 1L, 1L, 1L, 1L);
        acquire(USER, saved.get(4), LocalDateTime.of(2026, 9, 1, 12, 0));

        Page<Spot> p0 = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(0, 2));
        Page<Spot> p1 = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(1, 2));
        Page<Spot> p2 = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(2, 2));

        assertThat(p0.getTotalElements()).isEqualTo(5);
        assertThat(p0.getTotalPages()).isEqualTo(3);
        assertThat(p0.hasNext()).isTrue();
        assertThat(p2.hasNext()).isFalse();
        assertThat(List.of(p0, p1, p2))
                .flatExtracting(Page::getContent)
                .extracting(Spot::getId)
                .doesNotHaveDuplicates()
                .hasSize(5);
    }

    @Test
    void otherUsersStamps_doNotAffectOrderOrDuplicateRows() {
        List<Spot> saved = saveSpots(1L, 1L, 1L);
        LocalDateTime at = LocalDateTime.of(2026, 9, 1, 12, 0);
        acquire(OTHER_USER, saved.get(2), at);
        acquire(USER, saved.get(1), at);

        Page<Spot> page = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Spot::getId)
                .containsExactly(saved.get(1).getId(), saved.get(0).getId(), saved.get(2).getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void regionFilter_appliesSameOrderingWithinRegion() {
        List<Spot> saved = saveSpots(1L, 2L, 1L, 2L, 1L);
        Spot r1a = saved.get(0);
        Spot r1b = saved.get(2);
        Spot r1c = saved.get(4);
        acquire(USER, r1c, LocalDateTime.of(2026, 9, 1, 12, 0));

        Page<Spot> page =
                userStamps.findSpotsByRegionAcquiredFirst(USER, 1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Spot::getId)
                .containsExactly(r1c.getId(), r1a.getId(), r1b.getId());
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void withNoStamps_orderIsSpotIdAscending() {
        List<Spot> saved = saveSpots(1L, 1L, 1L);

        Page<Spot> page = userStamps.findSpotsAcquiredFirst(USER, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Spot::getId)
                .containsExactly(
                        saved.get(0).getId(), saved.get(1).getId(), saved.get(2).getId());
    }

    private List<Spot> saveSpots(Long... regionIds) {
        List<Spot> result = new java.util.ArrayList<>();
        for (int i = 0; i < regionIds.length; i++) {
            result.add(
                    spots.saveAndFlush(
                            Spot.builder()
                                    .regionId(regionIds[i])
                                    .nameKo("스팟" + i)
                                    .category(SpotCategory.K_DRAMA)
                                    .placeType(PlaceType.OTHER)
                                    .addressKo("주소" + i)
                                    .latitude(new BigDecimal("37.50000000"))
                                    .longitude(new BigDecimal("127.00000000"))
                                    .build()));
        }
        return result;
    }

    private void acquire(long userId, Spot spot, LocalDateTime acquiredAt) {
        Stamp stamp =
                stamps.findBySpotId(spot.getId())
                        .orElseGet(
                                () ->
                                        stamps.saveAndFlush(
                                                Stamp.builder().spotId(spot.getId()).build()));
        userStamps.saveAndFlush(
                UserStamp.builder()
                        .userId(userId)
                        .stampId(stamp.getId())
                        .spotId(spot.getId())
                        .regionId(spot.getRegionId())
                        .acquiredAt(acquiredAt)
                        .build());
    }
}
