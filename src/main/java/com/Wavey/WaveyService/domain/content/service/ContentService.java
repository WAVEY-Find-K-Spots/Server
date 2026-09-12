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

    @Transactional
    public ContentResponse createYoutube(ContentRequest request) {
        return create(request);
    }

    @Transactional
    public ContentResponse createSpotify(ContentRequest request) {
        return create(request);
    }

    @Transactional(readOnly = true)
    public ContentResponse get(Long contentId) {
        return ContentResponse.from(getContent(contentId));
    }

    @Transactional
    public ContentResponse update(Long contentId, ContentRequest request) {
        Content content = getContent(contentId);
        String title = request.getTitleKo().trim();
        contentRepository.findByTitleKoAndCategory(title, request.getCategory())
                .filter(found -> !found.getContentId().equals(contentId))
                .ifPresent(found -> {
                    throw new CustomException(ErrorCode.WORK_ALREADY_EXISTS);
                });

        content.update(title, trimToNull(request.getTitleEn()), request.getCategory());
        return ContentResponse.from(content);
    }

    @Transactional
    public ContentResponse updateYoutube(Long contentId, ContentRequest request) {
        return update(contentId, request);
    }

    @Transactional
    public ContentResponse updateSpotify(Long contentId, ContentRequest request) {
        return update(contentId, request);
    }

    @Transactional
    public void delete(Long contentId) {
        contentRepository.delete(getContent(contentId));
    }

    @Transactional
    public void deleteYoutube(Long contentId) {
        delete(contentId);
    }

    @Transactional
    public void deleteSpotify(Long contentId) {
        delete(contentId);
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> list(ContentCategory category) {
        List<Content> contents = category == null
                ? contentRepository.findAllByOrderByIdDesc()
                : contentRepository.findByCategoryOrderByIdDesc(category);
        return contents.stream().map(ContentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> searchYoutube(String keyword) {
        return search(keyword);
    }

    @Transactional(readOnly = true)
    public List<ContentResponse> searchSpotify(String keyword) {
        return search(keyword);
    }

    @Transactional(readOnly = true)
    public Content getContent(Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_NOT_FOUND));
    }

    private List<ContentResponse> search(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return contentRepository.findAllByOrderByIdDesc().stream()
                .filter(content -> containsIgnoreCase(content.getTitleKo(), normalizedKeyword)
                        || containsIgnoreCase(content.getTitleEn(), normalizedKeyword))
                .map(ContentResponse::from)
                .toList();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }
}
