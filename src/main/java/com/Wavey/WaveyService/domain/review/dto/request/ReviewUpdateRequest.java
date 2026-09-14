package com.Wavey.WaveyService.domain.review.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(

        @DecimalMin(value = "1.0", message = "평점은 최소 1점입니다.")
        @DecimalMax(value = "5.0", message = "평점은 최대 5점입니다.")
        Double rating,

        @Size(
                max = 2000,
                message = "리뷰는 최대 2000자까지 작성할 수 있습니다."
        )
        String body

) {

    @AssertTrue(message = "수정할 내용을 입력해주세요.")
    public boolean hasChanges() {
        return rating != null || body != null;
    }

    @AssertTrue(message = "리뷰 내용은 공백으로만 작성할 수 없습니다.")
    public boolean isBodyValid() {
        return body == null || !body.isBlank();
    }
}
