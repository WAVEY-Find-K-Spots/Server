package com.Wavey.WaveyService.domain.user.controller;

import com.Wavey.WaveyService.domain.spot.enums.SpotCategory;
import com.Wavey.WaveyService.domain.user.entity.*;
import com.Wavey.WaveyService.domain.user.repository.AppDocumentRepository;
import com.Wavey.WaveyService.global.common.UiSupport;
import com.Wavey.WaveyService.global.exception.*;
import com.Wavey.WaveyService.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/app")
public class AppMetadataController {
    private final AppDocumentRepository documents;

    @Value("${wavey.app-version:1.0.0}")
    private String version;

    public record Category(String value, String label) {}

    public record Metadata(
            String version,
            String mapProvider,
            List<String> languages,
            List<Category> categories,
            List<String> sorts) {}

    @GetMapping("/metadata")
    public ApiResponse<Metadata> metadata(
            @AuthenticationPrincipal User u, @RequestParam(required = false) String language) {
        String lang = UiSupport.language(language);
        return ApiResponse.success(
                "앱 정보",
                new Metadata(
                        version,
                        "GOOGLE_MAPS",
                        List.of("ko", "en"),
                        Arrays.stream(SpotCategory.values())
                                .map(
                                        c ->
                                                new Category(
                                                        UiSupport.categoryCode(c),
                                                        UiSupport.categoryLabel(c, lang)))
                                .toList(),
                        List.of("POPULAR", "RATING", "LATEST", "DISTANCE")));
    }

    @GetMapping("/documents/{type}")
    public ApiResponse<AppDocument> document(
            @PathVariable String type,
            @AuthenticationPrincipal User u,
            @RequestParam(required = false) String language) {
        if (!Set.of("PRIVACY", "TERMS").contains(type))
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        String lang = UiSupport.language(language);
        return ApiResponse.success(
                "약관",
                documents
                        .findFirstByTypeAndLanguageAndEffectiveAtLessThanEqualOrderByEffectiveAtDesc(
                                type, lang, LocalDateTime.now())
                        .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND)));
    }
}
