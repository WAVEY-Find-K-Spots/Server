package com.Wavey.WaveyService.domain.review.dto.response;

import java.util.List;

public record MyReviewListResponse(

        List<ReviewResponse> reviews,

        int page,
        int size,
        boolean hasNext

) {
}