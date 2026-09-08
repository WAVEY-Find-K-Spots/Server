package com.Wavey.WaveyService.domain.docent.repository;

import com.Wavey.WaveyService.domain.docent.model.CulturalTerm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KoreanBasicDictionaryDatasetRepositoryTest {

    @Test
    void 정규화된_한국어기초사전_JSON을_불러와_문장에_포함된_용어를_찾는다() {
        KoreanBasicDictionaryDatasetRepository repository = repository(
                "classpath:dictionary/test-korean-basic-dictionary.json"
        );

        List<CulturalTerm> result = repository.findCandidates("광장시장에서 육회를 먹었습니다.");

        assertThat(result)
                .extracting(CulturalTerm::korean)
                .containsExactly("광장시장", "육회");
    }

    @Test
    void 서비스용_통합_데이터셋에서_한식_공식명과_설명을_찾는다() {
        KoreanBasicDictionaryDatasetRepository repository = repository(
                "classpath:datasets/translation-terms.json"
        );

        CulturalTerm yukhoe = repository.findCandidates("육회를 주문합니다.").stream()
                .filter(term -> term.korean().equals("육회"))
                .findFirst()
                .orElseThrow();

        assertThat(yukhoe.domain()).isEqualTo("FOOD");
        assertThat(yukhoe.english()).isNotBlank();
        assertThat(yukhoe.englishDescription()).isNotBlank();
        assertThat(yukhoe.translationSource()).isEqualTo("OFFICIAL_DATASET");
    }

    private KoreanBasicDictionaryDatasetRepository repository(String datasetPath) {
        KoreanBasicDictionaryDatasetRepository repository =
                new KoreanBasicDictionaryDatasetRepository(
                        new ObjectMapper(),
                        new DefaultResourceLoader()
                );
        ReflectionTestUtils.setField(repository, "datasetPath", datasetPath);
        repository.loadDataset();
        return repository;
    }
}