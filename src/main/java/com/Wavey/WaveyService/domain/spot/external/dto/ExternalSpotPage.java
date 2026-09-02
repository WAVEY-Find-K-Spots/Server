package com.Wavey.WaveyService.domain.spot.external.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalSpotPage {

    private List<ExternalSpotPayload> items;
    private int page;
    private int size;
    private int totalCount;

    public boolean hasNextPage() {
        return page * size < totalCount;
    }
}
