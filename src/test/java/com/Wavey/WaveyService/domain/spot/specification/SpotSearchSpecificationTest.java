package com.Wavey.WaveyService.domain.spot.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.Wavey.WaveyService.domain.route.entity.Route;
import com.Wavey.WaveyService.domain.route.entity.RouteSpot;
import com.Wavey.WaveyService.domain.route.entity.Visibility;
import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.entity.Spot;
import com.Wavey.WaveyService.domain.spot.enums.PlaceType;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ContextConfiguration(classes = com.Wavey.WaveyService.WaveyServiceApplication.class)
class SpotSearchSpecificationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    private Spot spot(String name) {
        return Spot.builder()
                .regionId(1L)
                .nameKo(name)
                .category(SpotCategory.K_DRAMA)
                .placeType(PlaceType.OTHER)
                .addressKo("서울시 어딘가")
                .latitude(BigDecimal.valueOf(37.5))
                .longitude(BigDecimal.valueOf(127.0))
                .build();
    }

    @Test
    void excludeRouteId로_이미_루트에_담긴_스팟은_제외된다() {
        Spot includedSpot = testEntityManager.persistAndFlush(spot("포함될 스팟"));
        Spot excludedSpot = testEntityManager.persistAndFlush(spot("제외될 스팟"));

        Route route = testEntityManager.persistAndFlush(
                Route.builder()
                        .userId(1L)
                        .name("테스트 루트")
                        .visibility(Visibility.PRIVATE)
                        .build());

        testEntityManager.persistAndFlush(
                RouteSpot.builder()
                        .route(route)
                        .spotId(excludedSpot.getId())
                        .sequenceOrder(1)
                        .build());

        SpotSearchRequest request = new SpotSearchRequest(
                null, null, null, null, null, null, null, null, null, null, route.getId());

        var spec = SpotSearchSpecification.from(request);
        CriteriaBuilder cb = testEntityManager.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Spot> cq = cb.createQuery(Spot.class);
        Root<Spot> root = cq.from(Spot.class);
        cq.where(spec.toPredicate(root, cq, cb));

        List<Spot> result = testEntityManager.getEntityManager().createQuery(cq).getResultList();

        assertThat(result)
                .extracting(Spot::getId)
                .containsExactly(includedSpot.getId());
    }
}
