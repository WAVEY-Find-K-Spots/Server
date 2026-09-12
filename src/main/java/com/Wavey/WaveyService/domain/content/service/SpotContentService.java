package com.Wavey.WaveyService.domain.content.service;

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

    public record Item(Long contentId, ContentCategory category, String title) {}
    public record Result(List<Item> dramas, List<Item> movies, List<Item> music, List<Item> videos) {}

    public Result get(Long spotId, Long userId, String language) {
        if (!spots.existsById(spotId)) {
            throw new CustomException(ErrorCode.SPOT_NOT_FOUND);
        }
        List<Item> all = new ArrayList<>();
        links.findBySpotIdOrderByIdAsc(spotId).forEach(link ->
                contents.findById(link.getContentId())
                        .ifPresent(content -> all.add(new Item(content.getContentId(), content.getCategory(), content.getTitle()))));
        return new Result(
                filter(all, ContentCategory.DRAMA),
                filter(all, ContentCategory.MOVIE),
                filter(all, ContentCategory.ARTIST),
                List.copyOf(all));
    }

    private List<Item> filter(List<Item> items, ContentCategory category) {
        return items.stream().filter(item -> item.category() == category).toList();
    }
}
