package com.Wavey.WaveyService.domain.content.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotMediaResponse {

    private Long spotId;
    private List<ContentMediaBundleResponse> contents;
}
