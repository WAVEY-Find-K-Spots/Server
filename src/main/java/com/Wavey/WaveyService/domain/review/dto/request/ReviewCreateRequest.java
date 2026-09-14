package com.Wavey.WaveyService.domain.review.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(

        @NotNull(message = "평점은 필수입니다.")
        @DecimalMin(value = "1.0", message = "평점은 최소 1점입니다.")
        @DecimalMax(value = "5.0", message = "평점은 최대 5점입니다.")
        Double rating,

        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(
                max = 2000,
                message = "리뷰는 최대 2000자까지 작성할 수 있습니다."
        )
        String body

) {
}
