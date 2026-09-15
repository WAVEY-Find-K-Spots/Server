package com.Wavey.WaveyService.domain.spot.sync.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpotSyncPage {
    private List<SpotSyncPayload> items;
    private int page;
    private int size;
    private int totalCount;

    public boolean hasNextPage() {
        return page * size < totalCount;
    }
}
