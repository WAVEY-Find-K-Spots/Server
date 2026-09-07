package com.Wavey.WaveyService.domain.docent.repository;

import com.Wavey.WaveyService.domain.docent.model.HeritageRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HeritageDatasetRepository implements HeritageRepository {

    private static final int MAX_RESULTS = 5;

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${heritage.dataset-path:classpath:datasets/heritage-list.json}")
    private String datasetPath;

    private List<HeritageRecord> records = List.of();

    @PostConstruct
    void loadDataset() {
        Resource resource = resourceLoader.getResource(datasetPath);
        if (!resource.exists()) {
            log.warn("국가유산 데이터셋을 찾을 수 없습니다: {}", datasetPath);
            return;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            records = objectMapper.readValue(inputStream, new TypeReference<List<HeritageRecord>>() { });
            log.info("식별 가능한 국가유산 {}개를 불러왔습니다.", records.size());
        } catch (Exception exception) {
            log.error("국가유산 데이터셋을 읽지 못했습니다: {}", datasetPath, exception);
            records = List.of();
        }
    }

    @Override
    public List<HeritageRecord> findCandidates(
            String rawText, List<String> visionCandidates, String region1, String region2
    ) {
        List<String> normalizedVisionCandidates = visionCandidates == null
                ? List.of()
                : visionCandidates.stream().map(this::normalize).filter(value -> !value.isBlank()).toList();
        String normalizedText = normalize(rawText);

        List<ScoredHeritage> matches = new ArrayList<>();
        for (HeritageRecord record : records) {
            int score = locationScore(record, region1, region2);
            if (score < 0) {
                continue;
            }
            String koreanName = normalize(record.koreanName());
            String englishName = normalize(record.displayEnglishName());
            int nameScore = nameScore(normalizedText, normalizedVisionCandidates, koreanName, englishName);
            if (nameScore > 0) {
                matches.add(new ScoredHeritage(record, score + nameScore));
            }
        }
        return matches.stream()
                .sorted(Comparator.comparingInt(ScoredHeritage::score).reversed())
                .limit(MAX_RESULTS)
                .map(ScoredHeritage::record)
                .toList();
    }

    private int locationScore(HeritageRecord record, String region1, String region2) {
        int score = 0;
        if (region1 != null && !region1.isBlank()) {
            if (!normalize(record.canonicalRegion1()).equals(normalize(region1))) {
                return -1;
            }
            score += 25;
        }
        if (region2 != null && !region2.isBlank()) {
            if (!normalize(record.canonicalRegion2()).equals(normalize(region2))) {
                return -1;
            }
            score += 15;
        }
        return score;
    }

    private int nameScore(
            String rawText, List<String> visionCandidates, String koreanName, String englishName
    ) {
        int score = 0;
        if (!koreanName.isBlank() && rawText.contains(koreanName)) {
            score = 80;
        }
        if (!englishName.isBlank() && rawText.contains(englishName)) {
            score = Math.max(score, 80);
        }
        for (String candidate : visionCandidates) {
            if (candidate.equals(koreanName) || (!englishName.isBlank() && candidate.equals(englishName))) {
                score = Math.max(score, 100);
            } else if (candidate.length() >= 3
                    && ((!koreanName.isBlank() && (candidate.contains(koreanName) || koreanName.contains(candidate)))
                    || (!englishName.isBlank() && (candidate.contains(englishName) || englishName.contains(candidate))))) {
                score = Math.max(score, 70);
            }
        }
        return score;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private record ScoredHeritage(HeritageRecord record, int score) {
    }
}
