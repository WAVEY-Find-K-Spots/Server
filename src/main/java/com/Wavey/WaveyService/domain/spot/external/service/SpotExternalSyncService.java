package com.Wavey.WaveyService.domain.spot.external.service;

import com.Wavey.WaveyService.domain.spot.dto.response.SpotSyncResponse;
import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;

public interface SpotExternalSyncService {

    SpotSyncResponse syncHeritageSpots(int pageNo, int numOfRows);

    SpotSyncResponse syncAllHeritageSpots(int numOfRows);

    SpotSyncResponse syncMediaLocationSpots(SpotCategory category, int page, int perPage);

    SpotSyncResponse syncAllMediaLocationSpots(SpotCategory category, int perPage);

    SpotSyncResponse fillMediaLocationThumbnails(SpotCategory category, int limit);
}
