package com.Wavey.WaveyService.domain.docent.service;

import com.Wavey.WaveyService.domain.docent.client.TranslationClient;
import com.Wavey.WaveyService.domain.docent.dto.TranslationResponse;
import com.Wavey.WaveyService.domain.docent.model.CulturalTerm;
import com.Wavey.WaveyService.domain.docent.repository.CulturalTermRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private CulturalTermRepository culturalTermRepository;
    @Mock
    private TranslationClient translationClient;

    @InjectMocks
    private TranslationService translationService;

    @Test
    void 일반_문장은_조사_경계의_공식_용어를_치환하고_Glossary_없이_번역한다() {
        String normalizedText = "광장시장에서 육회를 먹었습니다.";
        CulturalTerm market = term("시장", "market", "CULTURAL_TERM", false);
        CulturalTerm gwangjangMarket = term(
                "광장시장", "Gwangjang Market", "CULTURAL_TERM", false
        );
        CulturalTerm yukhoe = term("육회", "Beef Tartare", "FOOD", false);

        when(culturalTermRepository.findCandidates(normalizedText))
                .thenReturn(List.of(market, gwangjangMarket, yukhoe));
        when(translationClient.translateKoreanToEnglish(
                List.of("Gwangjang Market에서 Beef Tartare를 먹었습니다."), false
        )).thenReturn(List.of("I ate Beef Tartare at Gwangjang Market."));

        TranslationResponse response = translationService.process(
                "  광장시장에서 육회를 먹었습니다.  "
        );

        assertThat(response.originalText()).isEqualTo(normalizedText);
        assertThat(response.translatedText())
                .isEqualTo("I ate Beef Tartare at Gwangjang Market.");
        assertThat(response.terms())
                .extracting(term -> term.original())
                .containsExactly("광장시장", "육회");
        verify(translationClient).translateKoreanToEnglish(
                List.of("Gwangjang Market에서 Beef Tartare를 먹었습니다."), false
        );
    }

    @Test
    void 메뉴판은_FOOD_도메인의_줄_전체_일치_용어만_설명에_포함한다() {
        String text = """
                김치찌개
                국산 묵은지와 돼지고기가
                7,000원
                청국장
                소주 4,000원
                만두사리 2,000원
                """;
        String normalizedText = text.strip();
        CulturalTerm kimchiStew = term("김치찌개", "Kimchi Stew", "FOOD", false);
        CulturalTerm cheonggukjang = term("청국장", "Cheonggukjang", "FOOD", false);
        List<CulturalTerm> falseCulturalMatches = List.of(
                term("가", "ritual wine vessel", "CULTURAL_TERM", false),
                term("원", "inn", "CULTURAL_TERM", false),
                term("장", "chest", "CULTURAL_TERM", true),
                term("주", "helmet", "CULTURAL_TERM", false),
                term("사리", "relics of Buddha", "CULTURAL_TERM", false)
        );

        when(culturalTermRepository.findCandidates(normalizedText)).thenReturn(List.of(
                kimchiStew,
                cheonggukjang,
                falseCulturalMatches.get(0),
                falseCulturalMatches.get(1),
                falseCulturalMatches.get(2),
                falseCulturalMatches.get(3),
                falseCulturalMatches.get(4)
        ));
        List<String> googleInputs = List.of(
                "국산 묵은지와 돼지고기가",
                "7,000원",
                "소주 4,000원",
                "만두사리 2,000원"
        );
        when(translationClient.translateKoreanToEnglish(googleInputs, false)).thenReturn(List.of(
                "Domestically sourced aged kimchi and pork",
                "7,000 won",
                "Soju 4,000 won",
                "Dumpling add-on 2,000 won"
        ));

        TranslationResponse response = translationService.process(text);

        assertThat(response.translatedText()).isEqualTo("""
                Kimchi Stew
                Domestically sourced aged kimchi and pork
                7,000 won
                Cheonggukjang
                Soju 4,000 won
                Dumpling add-on 2,000 won""");
        assertThat(response.terms())
                .extracting(term -> term.original())
                .containsExactly("김치찌개", "청국장");
        verify(translationClient).translateKoreanToEnglish(googleInputs, false);
    }

    @Test
    void 일반_단어_내부의_짧은_문화재_용어는_선택하지_않는다() {
        String text = "돼지고기가 7,000원이고 청국장과 소주, 만두사리가 있습니다.";
        List<CulturalTerm> falseMatches = List.of(
                term("가", "ritual wine vessel", "CULTURAL_TERM", false),
                term("원", "inn", "CULTURAL_TERM", false),
                term("장", "chest", "CULTURAL_TERM", true),
                term("주", "helmet", "CULTURAL_TERM", false),
                term("사리", "relics of Buddha", "CULTURAL_TERM", false)
        );
        when(culturalTermRepository.findCandidates(text)).thenReturn(falseMatches);
        when(translationClient.translateKoreanToEnglish(List.of(text), false))
                .thenReturn(List.of("Pork costs 7,000 won."));

        TranslationResponse response = translationService.process(text);

        assertThat(response.translatedText()).isEqualTo("Pork costs 7,000 won.");
        assertThat(response.terms()).isEmpty();
        verify(translationClient).translateKoreanToEnglish(List.of(text), false);
    }

    @Test
    void OCR_원문이_비어있으면_외부_연동을_호출하지_않는다() {
        TranslationResponse response = translationService.process("  \n ");

        assertThat(response.originalText()).isEmpty();
        assertThat(response.translatedText()).isEmpty();
        assertThat(response.terms()).isEmpty();
        verifyNoInteractions(culturalTermRepository, translationClient);
    }

    @Test
    void 삼만일_code_point_원문은_API_제한_이하로_분할해_순서대로_결합한다() {
        String firstChunk = "가".repeat(30_000);
        String secondChunk = "나";
        String text = firstChunk + secondChunk;
        when(culturalTermRepository.findCandidates(text)).thenReturn(List.of());
        when(translationClient.translateKoreanToEnglish(List.of(firstChunk), false))
                .thenReturn(List.of("FIRST"));
        when(translationClient.translateKoreanToEnglish(List.of(secondChunk), false))
                .thenReturn(List.of("SECOND"));

        TranslationResponse response = translationService.process(text);

        assertThat(response.translatedText()).isEqualTo("FIRSTSECOND");
        verify(translationClient).translateKoreanToEnglish(List.of(firstChunk), false);
        verify(translationClient).translateKoreanToEnglish(List.of(secondChunk), false);
        verifyNoMoreInteractions(translationClient);
    }

    private CulturalTerm term(
            String korean,
            String english,
            String domain,
            boolean ambiguous
    ) {
        return new CulturalTerm(
                korean,
                english,
                korean + " 설명",
                english + " description",
                "TEST",
                "TEST_DATASET",
                domain,
                "OFFICIAL_DATASET",
                "OFFICIAL_DATASET",
                ambiguous
        );
    }
}
