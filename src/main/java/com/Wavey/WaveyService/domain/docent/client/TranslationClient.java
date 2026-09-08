package com.Wavey.WaveyService.domain.docent.client;

import java.util.List;

/**
 * 번역 공급자를 교체하거나 외부 호출 없이 Mock으로 테스트할 수 있도록 둔 내부 경계입니다.
 */
public interface TranslationClient {

    String translateKoreanToEnglish(String text, boolean glossaryRequired);

    default List<String> translateKoreanToEnglish(List<String> texts, boolean glossaryRequired) {
        return texts.stream()
                .map(text -> translateKoreanToEnglish(text, glossaryRequired))
                .toList();
    }
}