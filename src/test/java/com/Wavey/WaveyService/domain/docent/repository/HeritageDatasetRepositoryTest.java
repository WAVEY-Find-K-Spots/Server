package com.Wavey.WaveyService.domain.docent.repository;

import com.Wavey.WaveyService.domain.docent.model.HeritageRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeritageDatasetRepositoryTest {

    @Test
    void 서비스용_국가유산_목록을_읽고_OCR_명칭으로_검색한다() {
        HeritageDatasetRepository repository = new HeritageDatasetRepository(
                new ObjectMapper(),
                new DefaultResourceLoader()
        );
        ReflectionTestUtils.setField(
                repository,
                "datasetPath",
                "classpath:datasets/heritage-list.json"
        );
        repository.loadDataset();

        List<HeritageRecord> result = repository.findCandidates(
                "서울 숭례문",
                List.of(),
                "서울특별시",
                "중구"
        );

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().id()).isEqualTo("KHS-00001");
        assertThat(result.getFirst().officialEnglishName()).isEqualTo("Sungnyemun Gate, Seoul");
    }

    @Test
    void 다른_행정구역이면_같은_OCR_명칭도_후보에서_제외한다() {
        HeritageDatasetRepository repository = new HeritageDatasetRepository(
                new ObjectMapper(),
                new DefaultResourceLoader()
        );
        ReflectionTestUtils.setField(
                repository,
                "datasetPath",
                "classpath:datasets/heritage-list.json"
        );
        repository.loadDataset();

        assertThat(repository.findCandidates(
                "서울 숭례문",
                List.of(),
                "부산광역시",
                "중구"
        )).isEmpty();
    }
}
