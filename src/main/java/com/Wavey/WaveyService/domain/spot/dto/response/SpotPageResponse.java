package com.Wavey.WaveyService.domain.spot.dto.response;

import java.util.List;

public record SpotPageResponse(
        List<SpotListResponse> spots,
        int page,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}