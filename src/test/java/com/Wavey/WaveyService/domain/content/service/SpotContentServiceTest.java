package com.Wavey.WaveyService.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.Wavey.WaveyService.domain.content.dto.SpotContentItemResponse;
import com.Wavey.WaveyService.domain.content.entity.Content;
import com.Wavey.WaveyService.domain.content.entity.ContentCategory;
import com.Wavey.WaveyService.domain.content.entity.SpotContent;
import com.Wavey.WaveyService.domain.content.repository.ContentRepository;
import com.Wavey.WaveyService.domain.content.repository.SpotContentRepository;
import com.Wavey.WaveyService.domain.spot.repository.SpotRepository;
import com.Wavey.WaveyService.global.exception.CustomException;
import com.Wavey.WaveyService.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SpotContentServiceTest {

    @Mock
    private SpotContentRepository links;
    @Mock
    private ContentRepository contents;
    @Mock
    private SpotRepository spots;

    @InjectMocks
    private SpotContentService service;

    @Test
    void list_all_returnsEveryLinkedContent() {
        stubThreeLinks();

        List<SpotContentItemResponse> result = service.list(10L, null);

        assertThat(result).extracting(SpotContentItemResponse::contentId).containsExactly(101L, 102L, 103L);
    }

    @Test
    void list_byCategory_filters() {
        stubThreeLinks();

        List<SpotContentItemResponse> result = service.list(10L, ContentCategory.DRAMA);

        assertThat(result).extracting(SpotContentItemResponse::contentId).containsExactly(101L);
        assertThat(result).extracting(SpotContentItemResponse::category).containsOnly(ContentCategory.DRAMA);
    }

    @Test
    void list_missingSpot_throwsNotFound() {
        given(spots.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> service.list(99L, null))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SPOT_NOT_FOUND);
    }

    private void stubThreeLinks() {
        given(spots.existsById(10L)).willReturn(true);
        given(links.findBySpotIdOrderByIdAsc(10L)).willReturn(List.of(
                link(1L, 101L),
                link(2L, 102L),
                link(3L, 103L)
        ));
        given(contents.findById(101L)).willReturn(Optional.of(content(101L, "도깨비", ContentCategory.DRAMA)));
        given(contents.findById(102L)).willReturn(Optional.of(content(102L, "기생충", ContentCategory.MOVIE)));
        given(contents.findById(103L)).willReturn(Optional.of(content(103L, "IU", ContentCategory.ARTIST)));
    }

    private static SpotContent link(Long id, Long contentId) {
        SpotContent link = SpotContent.builder().spotId(10L).contentId(contentId).build();
        ReflectionTestUtils.setField(link, "id", id);
        return link;
    }

    private static Content content(Long id, String titleKo, ContentCategory category) {
        Content content = Content.builder().titleKo(titleKo).category(category).build();
        ReflectionTestUtils.setField(content, "id", id);
        return content;
    }
}
