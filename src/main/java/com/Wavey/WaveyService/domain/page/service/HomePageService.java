package com.Wavey.WaveyService.domain.page.service;

import com.Wavey.WaveyService.domain.spot.dto.request.SpotSearchRequest;
import com.Wavey.WaveyService.domain.spot.dto.response.SpotPageResponse;
import com.Wavey.WaveyService.domain.spot.service.SpotSearchService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomePageService {

    private final SpotSearchService spotSearchService;

    public SpotPageResponse getHomeSpots(
            SpotSearchRequest request,
            Long userId
    ) {
        return spotSearchService.search(
                request,
                userId
        );
    }
}
