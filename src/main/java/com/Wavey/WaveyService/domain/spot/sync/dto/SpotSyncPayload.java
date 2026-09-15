package com.Wavey.WaveyService.domain.spot.sync.dto;

import com.Wavey.WaveyService.domain.spot.enums.ExternalSource;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class SpotSyncPayload {
    private ExternalSource source;
    private String externalId;
    private String externalContentTypeId;
    private String contentTitle;
    private String mediaType;
    private String nameKo;
    private String nameEn;
    private String placeType;
    private SpotCategory category;
    private String addressKo;
    private String addressEn;
    private String transportInfoKo;
    private String transportInfoEn;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String descriptionKo;
    private String descriptionEn;
    private String openingHours;
    private String breakTime;
    private String closedDaysKo;
    private String closedDaysEn;
    private String tel;
    private String imageUrl;
}
