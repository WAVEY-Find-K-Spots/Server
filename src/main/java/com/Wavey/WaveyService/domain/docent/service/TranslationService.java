package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.client.TranslationClient;
import com.Wavey.WaveyService.domain.docent.dto.CulturalTermResponse;
import com.Wavey.WaveyService.domain.docent.dto.TranslationResponse;
import com.Wavey.WaveyService.domain.docent.model.CulturalTerm;
import com.Wavey.WaveyService.domain.docent.repository.CulturalTermRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** OCR 문자열을 문맥에 맞는 관광 특화 영어 번역 결과로 변환하는 내부 서비스입니다. */
@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final String FOOD_DOMAIN = "FOOD";
    private static final Pattern PRICE_PATTERN = Pattern.compile("[0-9][0-9,]*\\s*원");
    private static final Pattern MENU_LINE_PATTERN = Pattern.compile(
            "^(.*?)(?:\\s+)?([0-9][0-9,]*\\s*원)?$"
    );
    private static final List<String> KOREAN_PARTICLES = List.of(
            "으로부터", "에게서", "이라고", "에서는", "으로는", "까지도", "에서", "으로",
            "에게", "께서", "처럼", "보다", "부터", "까지", "라고", "이며",
            "은", "는", "이", "가", "을", "를", "의", "에", "와", "과", "로", "도", "만", "나", "이나"
    );

    private final CulturalTermRepository culturalTermRepository;
    private final TranslationClient translationClient;

    public TranslationResponse process(String rawText) {
        String normalizedText = normalize(rawText);
        if (normalizedText.isBlank()) {
            return new TranslationResponse("", "", List.of());
        }

        List<CulturalTerm> candidates = culturalTermRepository.findCandidates(normalizedText);
        TranslationPlan plan = isMenu(normalizedText, candidates)
                ? createMenuPlan(normalizedText, candidates)
                : createProsePlan(normalizedText, candidates);
        String translatedText = translate(plan);
        List<CulturalTermResponse> termResponses = plan.terms().stream()
                .map(CulturalTermResponse::from)
                .toList();

        return new TranslationResponse(normalizedText, translatedText, termResponses);
    }

    private TranslationPlan createMenuPlan(String text, List<CulturalTerm> candidates) {
        Map<String, CulturalTerm> foodTerms = uniqueTerms(candidates).values().stream()
                .filter(term -> FOOD_DOMAIN.equals(term.domain()))
                .collect(Collectors.toMap(
                        CulturalTerm::korean,
                        term -> term,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        List<String> sourceLines = text.lines().toList();
        List<String> fixedTranslations = new ArrayList<>();
        List<String> googleInputs = new ArrayList<>();
        List<CulturalTerm> selectedTerms = new ArrayList<>();
        Set<String> addedTerms = new HashSet<>();

        for (String line : sourceLines) {
            MenuLine menuLine = parseMenuLine(line);
            CulturalTerm exactTerm = foodTerms.get(menuLine.label());
            if (exactTerm != null && hasOfficialEnglish(exactTerm)) {
                fixedTranslations.add(exactTerm.english() + menuLine.englishPriceSuffix());
                googleInputs.add(null);
                if (addedTerms.add(exactTerm.korean())) {
                    selectedTerms.add(exactTerm);
                }
            } else {
                fixedTranslations.add(null);
                googleInputs.add(line);
            }
        }
        return new TranslationPlan(sourceLines, fixedTranslations, googleInputs, selectedTerms);
    }

    private TranslationPlan createProsePlan(String text, List<CulturalTerm> candidates) {
        List<TermMatch> matches = findBoundaryMatches(text, candidates);
        String preparedText = replaceWithOfficialNames(text, matches);
        List<CulturalTerm> selectedTerms = matches.stream()
                .map(TermMatch::term)
                .distinct()
                .toList();
        return new TranslationPlan(
                List.of(text),
                java.util.Collections.singletonList(null),
                List.of(preparedText),
                selectedTerms
        );
    }

    private String translate(TranslationPlan plan) {
        List<String> inputs = plan.googleInputs().stream()
                .filter(value -> value != null)
                .toList();
        List<String> translations = translationClient.translateKoreanToEnglish(inputs, false);
        if (translations.size() != inputs.size()) {
            throw new CustomException(ErrorCode.GOOGLE_TRANSLATION_FAILED);
        }

        List<String> translatedLines = new ArrayList<>(plan.sourceLines().size());
        int translationIndex = 0;
        for (int index = 0; index < plan.sourceLines().size(); index++) {
            String fixedTranslation = plan.fixedTranslations().get(index);
            translatedLines.add(
                    fixedTranslation == null ? translations.get(translationIndex++) : fixedTranslation
            );
        }
        return String.join("\n", translatedLines);
    }

    private boolean isMenu(String text, List<CulturalTerm> candidates) {
        long priceCount = PRICE_PATTERN.matcher(text).results().count();
        Set<String> foodNames = uniqueTerms(candidates).values().stream()
                .filter(term -> FOOD_DOMAIN.equals(term.domain()))
                .map(CulturalTerm::korean)
                .collect(Collectors.toSet());
        long exactFoodLineCount = text.lines()
                .map(this::parseMenuLine)
                .map(MenuLine::label)
                .filter(foodNames::contains)
                .count();
        return priceCount >= 2 || exactFoodLineCount >= 2;
    }

    private List<TermMatch> findBoundaryMatches(String text, List<CulturalTerm> candidates) {
        List<CulturalTerm> terms = uniqueTerms(candidates).values().stream()
                .filter(this::hasOfficialEnglish)
                .filter(term -> !term.ambiguous())
                .sorted(Comparator.comparingInt((CulturalTerm term) -> term.korean().length()).reversed())
                .toList();
        List<TermMatch> matches = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int start = index;
            CulturalTerm match = terms.stream()
                    .filter(term -> text.startsWith(term.korean(), start))
                    .filter(term -> hasLeftBoundary(text, start))
                    .filter(term -> hasRightBoundary(text, start + term.korean().length()))
                    .findFirst()
                    .orElse(null);
            if (match == null) {
                index++;
                continue;
            }
            int end = index + match.korean().length();
            matches.add(new TermMatch(index, end, match));
            index = end;
        }
        return matches;
    }

    private boolean hasLeftBoundary(String text, int start) {
        return start == 0 || !Character.isLetterOrDigit(text.charAt(start - 1));
    }

    private boolean hasRightBoundary(String text, int end) {
        if (end >= text.length() || !isKoreanSyllable(text.charAt(end))) {
            return true;
        }
        return KOREAN_PARTICLES.stream().anyMatch(particle -> {
            if (!text.startsWith(particle, end)) {
                return false;
            }
            int particleEnd = end + particle.length();
            return particleEnd >= text.length() || !isKoreanSyllable(text.charAt(particleEnd));
        });
    }

    private boolean isKoreanSyllable(char value) {
        return value >= '가' && value <= '힣';
    }

    private String replaceWithOfficialNames(String text, List<TermMatch> matches) {
        StringBuilder prepared = new StringBuilder();
        int cursor = 0;
        for (TermMatch match : matches) {
            prepared.append(text, cursor, match.start());
            prepared.append(match.term().english());
            cursor = match.end();
        }
        prepared.append(text, cursor, text.length());
        return prepared.toString();
    }

    private Map<String, CulturalTerm> uniqueTerms(List<CulturalTerm> candidates) {
        if (candidates == null) {
            return Map.of();
        }
        return candidates.stream()
                .filter(term -> term != null && term.korean() != null && !term.korean().isBlank())
                .collect(Collectors.toMap(
                        CulturalTerm::korean,
                        term -> term,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }

    private boolean hasOfficialEnglish(CulturalTerm term) {
        return term.english() != null && !term.english().isBlank();
    }

    private MenuLine parseMenuLine(String line) {
        Matcher matcher = MENU_LINE_PATTERN.matcher(line.trim());
        if (!matcher.matches()) {
            return new MenuLine(line.trim(), "");
        }
        String label = matcher.group(1) == null ? "" : matcher.group(1).trim();
        String price = matcher.group(2);
        String englishPrice = price == null ? "" : " " + price.replaceAll("\\s*원$", " won");
        return new MenuLine(label, englishPrice);
    }

    private String normalize(String rawText) {
        if (rawText == null) {
            return "";
        }
        return rawText
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    private record TranslationPlan(
            List<String> sourceLines,
            List<String> fixedTranslations,
            List<String> googleInputs,
            List<CulturalTerm> terms
    ) { }

    private record TermMatch(int start, int end, CulturalTerm term) { }

    private record MenuLine(String label, String englishPriceSuffix) { }
}