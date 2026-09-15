package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.dto.SpotContentItemResponse;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.domain.content.repository.SpotContentRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpotContentService {
    private final SpotContentRepository links;
    private final ContentRepository contents;
    private final SpotRepository spots;

    public List<SpotContentItemResponse> list(Long spotId, ContentCategory category) {
        if (!spots.existsById(spotId)) {
            throw new CustomException(ErrorCode.SPOT_NOT_FOUND);
        }
        List<SpotContentItemResponse> items = new ArrayList<>();
        links.findBySpotIdOrderByIdAsc(spotId).forEach(link ->
                contents.findById(link.getContentId())
                        .ifPresent(content -> {
                            if (category == null || content.getCategory() == category) {
                                items.add(SpotContentItemResponse.from(content));
                            }
                        }));
        return items;
    }
}
