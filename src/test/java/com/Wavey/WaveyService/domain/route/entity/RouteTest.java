package com.Wavey.WaveyService.domain.route.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RouteTest {

    @Test
    void update시_null인_필드는_기존_값을_유지한다() {
        Route route = Route.builder()
                .userId(1L)
                .name("원래 이름")
                .description("원래 설명")
                .visibility(Visibility.PRIVATE)
                .build();

        route.update(null, null, Visibility.PUBLIC);

        assertThat(route.getName()).isEqualTo("원래 이름");
        assertThat(route.getDescription()).isEqualTo("원래 설명");
        assertThat(route.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void update시_전달된_필드만_변경된다() {
        Route route = Route.builder()
                .userId(1L)
                .name("원래 이름")
                .description("원래 설명")
                .visibility(Visibility.PRIVATE)
                .build();

        route.update("새 이름", null, null);

        assertThat(route.getName()).isEqualTo("새 이름");
        assertThat(route.getDescription()).isEqualTo("원래 설명");
        assertThat(route.getVisibility()).isEqualTo(Visibility.PRIVATE);
    }
}
