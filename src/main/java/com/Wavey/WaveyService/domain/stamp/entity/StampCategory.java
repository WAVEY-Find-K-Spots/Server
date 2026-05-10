package com.Wavey.WaveyService.domain.stamp.entity;

import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StampCategory {

    VISIT("방문"),
    REVIEW("리뷰");

    private final String value;

    @JsonCreator
    public static StampCategory from(String value) {
        return Arrays.stream(values())
                .filter(category -> category.value.equals(value) || category.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STAMP_INVALID_CATEGORY));
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
