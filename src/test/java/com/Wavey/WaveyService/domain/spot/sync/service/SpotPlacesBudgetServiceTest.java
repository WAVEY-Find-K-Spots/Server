package com.Wavey.WaveyService.domain.spot.sync.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SpotPlacesBudgetServiceTest {

    @Test
    void 한도_내에서는_소진량만큼_남은량이_줄어든다() {
        SpotPlacesBudgetService budget = new SpotPlacesBudgetService();
        ReflectionTestUtils.setField(budget, "monthlyPhotoLimit", 3);

        assertThat(budget.remaining()).isEqualTo(3);
        assertThat(budget.canUseOne()).isTrue();

        budget.recordUsed();
        budget.recordUsed();

        assertThat(budget.remaining()).isEqualTo(1);
        assertThat(budget.getUsedCount()).isEqualTo(2);
    }

    @Test
    void 한도_소진되면_더_이상_사용할_수_없다() {
        SpotPlacesBudgetService budget = new SpotPlacesBudgetService();
        ReflectionTestUtils.setField(budget, "monthlyPhotoLimit", 1);

        budget.recordUsed();

        assertThat(budget.remaining()).isEqualTo(0);
        assertThat(budget.canUseOne()).isFalse();
    }
}
