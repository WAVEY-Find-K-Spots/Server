package com.Wavey.WaveyService.domain.upload.enums;

import java.util.Map;

public enum UploadCategory {

    PROFILE("profile", Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    ));

    private final String keyPrefix;
    private final Map<String, String> allowedContentTypes;

    UploadCategory(String keyPrefix, Map<String, String> allowedContentTypes) {
        this.keyPrefix = keyPrefix;
        this.allowedContentTypes = allowedContentTypes;
    }

    public String keyPrefix() {
        return keyPrefix;
    }

    public String extensionFor(String contentType) {
        return allowedContentTypes.get(contentType);
    }
}
