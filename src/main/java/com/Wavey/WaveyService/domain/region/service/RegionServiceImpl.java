package com.Wavey.WaveyService.domain.region.service;

import com.Wavey.WaveyService.domain.region.converter.RegionConverter;
import com.Wavey.WaveyService.domain.region.dto.request.RegionCreateRequest;
import com.Wavey.WaveyService.domain.region.dto.request.RegionUpdateRequest;
import com.Wavey.WaveyService.domain.region.dto.response.RegionResponse;
import com.Wavey.WaveyService.domain.region.entity.Region;
import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    @Transactional
    public RegionResponse createRegion(RegionCreateRequest request) {
        Region region = regionRepository.save(RegionConverter.toEntity(request));
        return RegionConverter.toResponse(region);
    }

    @Override
    public RegionResponse getRegion(Long regionId) {
        return RegionConverter.toResponse(findRegion(regionId));
    }

    @Override
    public List<RegionResponse> getRegions() {
        return regionRepository.findAllByOrderByIdAsc().stream()
                .map(RegionConverter::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RegionResponse updateRegion(Long regionId, RegionUpdateRequest request) {
        Region region = findRegion(regionId);
        RegionConverter.updateEntity(region, request);
        return RegionConverter.toResponse(region);
    }

    @Override
    @Transactional
    public void deleteRegion(Long regionId) {
        regionRepository.delete(findRegion(regionId));
    }

    private Region findRegion(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
    }
}