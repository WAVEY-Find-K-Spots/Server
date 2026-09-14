package com.Wavey.WaveyService.domain.content.service;

import com.Wavey.WaveyService.domain.content.dto.ContentRequest;
import com.Wavey.WaveyService.domain.content.dto.ContentResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.repository.ContentAlbumRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentTrackRepository;
import com.Wavey.WaveyService.domain.content.repository.ContentVideoRepository;
import com.Wavey.WaveyService.domain.content.repository.SpotContentRepository;
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
    private final ContentVideoRepository contentVideoRepository;
    private final ContentAlbumRepository contentAlbumRepository;
    private final ContentTrackRepository contentTrackRepository;
    private final SpotContentRepository spotContentRepository;

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

    @Transactional
    public ContentResponse update(Long contentId, ContentRequest request) {
        Content content = getContent(contentId);
        String title = request.getTitleKo().trim();
        contentRepository.findByTitleKoAndCategory(title, request.getCategory())
                .filter(existing -> !existing.getContentId().equals(contentId))
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.WORK_ALREADY_EXISTS);
                });

        content.update(title, trimToNull(request.getTitleEn()), request.getCategory());
        return ContentResponse.from(content);
    }

    @Transactional
    public void delete(Long contentId) {
        getContent(contentId);
        // tracks → albums (FK), videos, spot links, then content
        contentTrackRepository.deleteByContentId(contentId);
        contentAlbumRepository.deleteByContentId(contentId);
        contentVideoRepository.deleteByContentId(contentId);
        spotContentRepository.deleteByContentId(contentId);
        contentRepository.deleteById(contentId);
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
