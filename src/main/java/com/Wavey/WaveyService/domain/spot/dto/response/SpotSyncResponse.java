package com.Wavey.WaveyService.domain.spot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotSyncResponse {

    private int requestedCount;
    private int totalCount;
    private int pageCount;
    private int savedCount;
    private int updatedCount;
    private int unchangedCount;
    private int skippedCount;
}
