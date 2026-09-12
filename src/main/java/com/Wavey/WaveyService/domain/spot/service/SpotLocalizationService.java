package com.Wavey.WaveyService.domain.spot.service;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SpotLocalizationService {

    public String localize(
            String korean,
            String english,
            Locale locale
    ) {
        if (locale != null
                && Locale.ENGLISH.getLanguage()
                .equals(locale.getLanguage())) {

            return hasText(english) ? english : korean;
        }

        return hasText(korean) ? korean : english;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}