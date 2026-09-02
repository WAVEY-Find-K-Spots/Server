package com.Wavey.WaveyService.domain.region.service;

import com.Wavey.WaveyService.domain.region.converter.RegionConverter;
import com.Wavey.WaveyService.domain.region.dto.request.RegionCreateRequest;
import com.Wavey.WaveyService.domain.region.dto.request.RegionUpdateRequest;
import com.Wavey.WaveyService.domain.region.dto.response.RegionResponse;
import com.Wavey.WaveyService.domain.region.entity.Region;
import com.Wavey.WaveyService.domain.region.repository.RegionRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    public RegionResponse createRegion(RegionCreateRequest request) {
        validateDuplicatedCode(request.getCode().trim());

        Region region = RegionConverter.toEntity(request);
        Region savedRegion = regionRepository.save(region);
        return RegionConverter.toResponse(savedRegion);
    }

    @Override
    @Transactional(readOnly = true)
    public RegionResponse getRegion(Long regionId) {
        Region region = findRegionById(regionId);
        return RegionConverter.toResponse(region);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponse> getRegions() {
        return regionRepository.findAllOrderByName()
                .stream()
                .map(RegionConverter::toResponse)
                .toList();
    }

    @Override
    public RegionResponse updateRegion(Long regionId, RegionUpdateRequest request) {
        Region region = findRegionById(regionId);

        if (StringUtils.hasText(request.getCode())
                && regionRepository.existsCodeExceptId(request.getCode().trim(), regionId)) {
            throw new CustomException(ErrorCode.REGION_ALREADY_EXISTS);
        }

        RegionConverter.updateEntity(region, request);
        return RegionConverter.toResponse(region);
    }

    @Override
    public void deleteRegion(Long regionId) {
        Region region = findRegionById(regionId);
        regionRepository.delete(region);
    }

    private Region findRegionById(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
    }

    private void validateDuplicatedCode(String code) {
        if (regionRepository.existsCode(code)) {
            throw new CustomException(ErrorCode.REGION_ALREADY_EXISTS);
        }
    }
}
