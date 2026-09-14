package com.Wavey.WaveyService.domain.review.dto.response;

import java.util.List;

public record ReviewListResponse(

        double averageRating,
        long reviewCount,

        List<ReviewResponse> reviews,

        int page,
        int size,
        boolean hasNext

) {
}