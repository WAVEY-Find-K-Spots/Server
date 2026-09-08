package com.Wavey.WaveyService.domain.route.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이동수단")
public enum TransportMode {

    WALK("도보"),
    TRANSIT("대중교통"),
    CAR("자동차");

    private final String label;

    TransportMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
