package com.Wavey.WaveyService.global.common;

import com.Wavey.WaveyService.global.exception.*;

import java.util.*;

public final class UiSupport {
    private UiSupport() {}

    public static String language(String value) {
        if (value == null) return "ko";
        if (!Set.of("ko", "en").contains(value))
            throw new CustomException(ErrorCode.COMMON_INVALID_PARAMETER);
        return value;
    }

    public static String localized(String ko, String en, String language) {
        return "en".equals(language) && en != null && !en.isBlank() ? en : ko;
    }
}
