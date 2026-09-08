package com.Wavey.WaveyService.domain.docent.repository;

import com.Wavey.WaveyService.domain.docent.model.CulturalTerm;
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
import java.util.Comparator;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class KoreanBasicDictionaryDatasetRepository implements CulturalTermRepository {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${translation.dictionary.dataset-path:classpath:datasets/translation-terms.json}")
    private String datasetPath;

    private List<CulturalTerm> terms = List.of();

    @PostConstruct
    void loadDataset() {
        if (datasetPath == null || datasetPath.isBlank()) {
            log.warn("번역 용어 데이터셋 경로가 설정되지 않아 빈 사전으로 시작합니다.");
            return;
        }
        Resource resource = resourceLoader.getResource(datasetPath);
        if (!resource.exists()) {
            log.warn("번역 용어 데이터셋을 찾을 수 없습니다: {}", datasetPath);
            return;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            terms = objectMapper.readValue(
                            inputStream, new TypeReference<List<CulturalTerm>>() { }
                    ).stream()
                    .filter(term -> term.korean() != null && !term.korean().isBlank())
                    .sorted(Comparator.comparingInt((CulturalTerm term) -> term.korean().length()).reversed())
                    .toList();
            log.info("공식 번역 용어 {}개를 불러왔습니다.", terms.size());
        } catch (Exception exception) {
            log.error("번역 용어 데이터셋을 읽지 못했습니다: {}", datasetPath, exception);
            terms = List.of();
        }
    }

    @Override
    public List<CulturalTerm> findCandidates(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return terms.stream().filter(term -> text.contains(term.korean())).toList();
    }
}
