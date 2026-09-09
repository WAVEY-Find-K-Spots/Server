package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    @Transactional
    public ContentResponse create(ContentRequest request) {
        String title = request.getTitleKo().trim();
        if (contentRepository.findByTitleKoAndCategory(title, request.getCategory()).isPresent()) {
            throw new CustomException(ErrorCode.WORK_ALREADY_EXISTS);
        }

        Content content = Content.builder()
                .titleKo(title)
                .titleEn(trimToNull(request.getTitleEn()))
                .category(request.getCategory())
                .build();

        return ContentResponse.from(contentRepository.save(content));
    }

    @Transactional(readOnly = true)
    public ContentResponse get(Long contentId) {
        return ContentResponse.from(getContent(contentId));
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> list(ContentCategory category) {
        List<Content> contents = category == null
                ? contentRepository.findAllByOrderByIdDesc()
                : contentRepository.findByCategoryOrderByIdDesc(category);
        return contents.stream().map(ContentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
