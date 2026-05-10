package com.Wavey.WaveyService.domain.stamp.service;

import com.Wavey.WaveyService.domain.stamp.dto.StampRequest;
import com.Wavey.WaveyService.domain.stamp.dto.StampResponse;
import com.Wavey.WaveyService.domain.stamp.entity.Stamp;
import com.Wavey.WaveyService.domain.stamp.entity.StampCategory;
import com.Wavey.WaveyService.domain.stamp.repository.StampRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StampService {

    private final StampRepository stampRepository;

    @Transactional
    public StampResponse createStamp(StampRequest request) {
        StampCategory category = StampCategory.from(request.getCategory());
        validateDuplicate(request.getSpotId(), category);

        Stamp stamp = Stamp.builder()
                .spotId(request.getSpotId())
                .imgLink(request.getImgLink().trim())
                .category(category)
                .name(request.getName().trim())
                .context(normalizeText(request.getContext()))
                .requirement(normalizeText(request.getRequirement()))
                .build();

        try {
            return toResponse(stampRepository.saveAndFlush(stamp));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.STAMP_ALREADY_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<StampResponse> getAllStamps() {
        return stampRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StampResponse getStampById(Long stampId) {
        return toResponse(getStamp(stampId));
    }

    @Transactional
    public StampResponse updateStamp(Long stampId, StampRequest request) {
        Stamp stamp = getStamp(stampId);
        StampCategory category = StampCategory.from(request.getCategory());
        validateDuplicateOnUpdate(stampId, request.getSpotId(), category);

        stamp.update(
                request.getSpotId(),
                request.getImgLink().trim(),
                category,
                request.getName().trim(),
                normalizeText(request.getContext()),
                normalizeText(request.getRequirement())
        );

        try {
            return toResponse(stampRepository.saveAndFlush(stamp));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.STAMP_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void deleteStamp(Long stampId) {
        stampRepository.delete(getStamp(stampId));
    }

    private Stamp getStamp(Long stampId) {
        return stampRepository.findById(stampId)
                .orElseThrow(() -> new CustomException(ErrorCode.STAMP_NOT_FOUND));
    }

    private void validateDuplicate(Long spotId, StampCategory category) {
        stampRepository.findBySpotIdAndCategory(spotId, category)
                .ifPresent(stamp -> {
                    throw new CustomException(ErrorCode.STAMP_ALREADY_EXISTS);
                });
    }

    private void validateDuplicateOnUpdate(Long stampId, Long spotId, StampCategory category) {
        stampRepository.findBySpotIdAndCategory(spotId, category)
                .filter(existing -> !existing.getStampId().equals(stampId))
                .ifPresent(stamp -> {
                    throw new CustomException(ErrorCode.STAMP_ALREADY_EXISTS);
                });
    }

    private String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private StampResponse toResponse(Stamp stamp) {
        return StampResponse.builder()
                .stampId(stamp.getStampId())
                .spotId(stamp.getSpotId())
                .imgLink(stamp.getImgLink())
                .category(stamp.getCategory())
                .name(stamp.getName())
                .context(stamp.getContext())
                .requirement(stamp.getRequirement())
                .createdAt(stamp.getCreated_at())
                .updatedAt(stamp.getUpdated_at())
                .build();
    }
}
